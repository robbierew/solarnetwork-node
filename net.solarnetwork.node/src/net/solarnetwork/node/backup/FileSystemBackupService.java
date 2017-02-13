/* ==================================================================
 * FileSystemBackupService.java - Mar 27, 2013 11:38:08 AM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.backup;

import static net.solarnetwork.node.backup.BackupStatus.Configured;
import static net.solarnetwork.node.backup.BackupStatus.Error;
import static net.solarnetwork.node.backup.BackupStatus.RunningBackup;
import static net.solarnetwork.node.backup.BackupStatus.Unconfigured;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.util.FileCopyUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.node.Constants;
import net.solarnetwork.node.IdentityService;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicSliderSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTitleSettingSpecifier;
import net.solarnetwork.util.OptionalService;

/**
 * {@link BackupService} implementation that copies files to another location in
 * the file system.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl class="class-properties">
 * <dt>backupDir</dt>
 * <dd>The directory to backup to.</dd>
 * 
 * <dt>additionalBackupCount</dt>
 * <dd>The number of additional backups to maintain. If greater than zero, then
 * this service will maintain this many copies of past backups.
 * </dl>
 * 
 * @author matt
 * @version 1.2
 */
public class FileSystemBackupService implements BackupService, SettingSpecifierProvider {

	private static final String ARCHIVE_NAME_DATE_FORMAT = "yyyyMMdd'T'HHmmss";

	/** The value returned by {@link #getKey()}. */
	public static final String KEY = FileSystemBackupService.class.getName();

	/**
	 * A format for turning a {@link Backup#getKey()} value into a zip file
	 * name.
	 */
	public static final String ARCHIVE_KEY_NAME_FORMAT = "node-%2$d-backup-%1$s.zip";

	private static final String ARCHIVE_NAME_FORMAT = "node-%2$d-backup-%1$tY%1$tm%1$tdT%1$tH%1$tM%1$tS.zip";
	private static final Pattern ARCHIVE_NAME_PAT = Pattern
			.compile("node-(\\d+)-backup-(\\d{8}T\\d{6})\\.zip");

	private final Logger log = LoggerFactory.getLogger(getClass());

	private MessageSource messageSource;
	private File backupDir = defaultBackuprDir();
	private OptionalService<IdentityService> identityService;
	private int additionalBackupCount = 1;
	private BackupStatus status = Configured;

	private static File defaultBackuprDir() {
		String path = System.getProperty(Constants.SYSTEM_PROP_NODE_HOME, null);
		if ( path == null ) {
			path = System.getProperty("java.io.tmpdir");
		} else {
			if ( !path.endsWith("/") ) {
				path += "/";
			}
			path += "var/backups";
		}
		return new File(path);
	}

	@Override
	public String getSettingUID() {
		return getClass().getName();
	}

	@Override
	public String getDisplayName() {
		return "File System Backup Service";
	}

	@Override
	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = new ArrayList<SettingSpecifier>(20);
		FileSystemBackupService defaults = new FileSystemBackupService();
		results.add(new BasicTitleSettingSpecifier("status", getStatus().toString(), true));
		results.add(new BasicTextFieldSettingSpecifier("backupDir",
				defaults.getBackupDir().getAbsolutePath()));
		results.add(new BasicSliderSettingSpecifier("additionalBackupCount",
				(double) defaults.getAdditionalBackupCount(), 0.0, 10.0, 1.0));
		return results;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public BackupServiceInfo getInfo() {
		return new SimpleBackupServiceInfo(null, getStatus());
	}

	private String getArchiveKey(String archiveName) {
		Matcher m = ARCHIVE_NAME_PAT.matcher(archiveName);
		if ( m.matches() ) {
			return m.group(2);
		}
		return archiveName;
	}

	@Override
	public Backup backupForKey(String key) {
		final File archiveFile = getArchiveFileForBackup(key);
		if ( !archiveFile.canRead() ) {
			return null;
		}
		return createBackupForFile(archiveFile, new SimpleDateFormat(ARCHIVE_NAME_DATE_FORMAT));
	}

	@Override
	public Backup performBackup(final Iterable<BackupResource> resources) {
		final Calendar now = new GregorianCalendar();
		now.set(Calendar.MILLISECOND, 0);
		return performBackupInternal(resources, now);
	}

	private Backup performBackupInternal(final Iterable<BackupResource> resources, final Calendar now) {
		if ( resources == null ) {
			return null;
		}
		final Iterator<BackupResource> itr = resources.iterator();
		if ( !itr.hasNext() ) {
			log.debug("No resources provided, nothing to backup");
			return null;
		}
		BackupStatus status = setStatusIf(RunningBackup, Configured);
		if ( status != RunningBackup ) {
			// try to reset from error
			status = setStatusIf(RunningBackup, Error);
			if ( status != RunningBackup ) {
				return null;
			}
		}
		if ( !backupDir.exists() ) {
			backupDir.mkdirs();
		}
		final Long nodeId = nodeIdForArchiveFileName();
		final String archiveName = String.format(ARCHIVE_NAME_FORMAT, now, nodeId);
		final File archiveFile = new File(backupDir, archiveName);
		final String archiveKey = getArchiveKey(archiveName);
		log.info("Starting backup to archive {}", archiveName);
		log.trace("Backup archive: {}", archiveFile.getAbsolutePath());
		Backup backup = null;
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archiveFile)));
			while ( itr.hasNext() ) {
				BackupResource r = itr.next();
				log.debug("Backup up resource {} to archive {}", r.getBackupPath(), archiveName);
				zos.putNextEntry(new ZipEntry(r.getBackupPath()));
				FileCopyUtils.copy(r.getInputStream(), new FilterOutputStream(zos) {

					@Override
					public void close() throws IOException {
						// FileCopyUtils closes the stream, which we don't want
					}

				});
			}
			zos.flush();
			zos.finish();
			log.info("Backup complete to archive {}", archiveName);
			backup = new SimpleBackup(now.getTime(), archiveKey, archiveFile.length(), true);

			// clean out older backups
			File[] backupFiles = getAvailableBackupFiles();
			if ( backupFiles != null && backupFiles.length > additionalBackupCount + 1 ) {
				// delete older files
				for ( int i = additionalBackupCount + 1; i < backupFiles.length; i++ ) {
					log.info("Deleting old backup archive {}", backupFiles[i].getName());
					if ( !backupFiles[i].delete() ) {
						log.warn("Unable to delete backup archive {}", backupFiles[i].getAbsolutePath());
					}
				}
			}
		} catch ( IOException e ) {
			log.error("IO error creating backup: {}", e.getMessage());
			setStatus(Error);
		} catch ( RuntimeException e ) {
			log.error("Error creating backup: {}", e.getMessage());
			setStatus(Error);
		} finally {
			if ( zos != null ) {
				try {
					zos.close();
				} catch ( IOException e ) {
					// ignore this
				}
			}
			status = setStatusIf(Configured, RunningBackup);
			if ( status != Configured ) {
				// clean up if we encountered an error
				if ( archiveFile.exists() ) {
					archiveFile.delete();
				}
			}
		}
		return backup;
	}

	private final Long nodeIdForArchiveFileName() {
		IdentityService service = (identityService != null ? identityService.service() : null);
		final Long nodeId = (service != null ? service.getNodeId() : null);
		return (nodeId != null ? nodeId : 0L);
	}

	private File getArchiveFileForBackup(final String backupKey) {
		final Long nodeId = nodeIdForArchiveFileName();
		if ( nodeId.intValue() == 0 ) {
			// hmm, might be restoring from corrupted db; look for file with matching key only
			File[] matches = backupDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.contains(backupKey);
				}
			});
			if ( matches != null && matches.length > 0 ) {
				// take first available
				return matches[0];
			}
			// not found
			return null;
		} else {
			return new File(backupDir, String.format(ARCHIVE_KEY_NAME_FORMAT, backupKey, nodeId));
		}
	}

	@Override
	public BackupResourceIterable getBackupResources(Backup backup) {
		final File archiveFile = getArchiveFileForBackup(backup.getKey());
		if ( !(archiveFile.isFile() && archiveFile.canRead()) ) {
			log.warn("No backup archive exists for key [{}]", backup.getKey());
			Collection<BackupResource> col = Collections.emptyList();
			return new CollectionBackupResourceIterable(col);
		}
		try {
			final ZipFile zf = new ZipFile(archiveFile);
			Enumeration<? extends ZipEntry> entries = zf.entries();
			List<BackupResource> result = new ArrayList<BackupResource>(20);
			while ( entries.hasMoreElements() ) {
				result.add(new ZipEntryBackupResource(zf, entries.nextElement()));
			}
			return new CollectionBackupResourceIterable(result) {

				@Override
				public void close() throws IOException {
					zf.close();
				}

			};
		} catch ( IOException e ) {
			log.error("Error extracting backup archive entries: {}", e.getMessage());
		}
		Collection<BackupResource> col = Collections.emptyList();
		return new CollectionBackupResourceIterable(col);
	}

	private Date backupDateFromProps(Date date, Map<String, String> props) {
		if ( date != null ) {
			return date;
		}
		final SimpleDateFormat sdf = new SimpleDateFormat(ARCHIVE_NAME_DATE_FORMAT);
		String backupKey = (props == null ? null : props.get(BackupManager.BACKUP_KEY));
		if ( backupKey != null ) {
			Matcher m = ARCHIVE_NAME_PAT.matcher(backupKey);
			if ( m.matches() ) {
				try {
					return sdf.parse(m.group(2));
				} catch ( ParseException e ) {
					log.warn("Unable to parse backup date from key [{}]", backupKey);
				}
			}
		}
		return new Date();
	}

	@Override
	public Backup importBackup(Date date, BackupResourceIterable resources, Map<String, String> props) {
		final Date backupDate = backupDateFromProps(date, props);
		final Calendar cal = new GregorianCalendar();
		cal.setTime(backupDate);
		cal.set(Calendar.MILLISECOND, 0);
		return performBackupInternal(resources, cal);
	}

	/**
	 * Delete any existing backups.
	 */
	public void removeAllBackups() {
		File[] archives = backupDir.listFiles(new ArchiveFilter(nodeIdForArchiveFileName()));
		if ( archives == null ) {
			return;
		}
		for ( File archive : archives ) {
			log.debug("Deleting backup archive {}", archive.getName());
			if ( !archive.delete() ) {
				log.warn("Unable to delete archive file {}", archive.getAbsolutePath());
			}
		}
	}

	/**
	 * Get all available backup files, ordered in desending backup order (newest
	 * to oldest).
	 * 
	 * @return ordered array of backup files, or <em>null</em> if directory does
	 *         not exist
	 */
	private File[] getAvailableBackupFiles() {
		File[] archives = backupDir.listFiles(new ArchiveFilter(nodeIdForArchiveFileName()));
		if ( archives != null ) {
			Arrays.sort(archives, new Comparator<File>() {

				@Override
				public int compare(File o1, File o2) {
					// sort in reverse order, so most recent backup first
					return o2.getName().compareTo(o1.getName());
				}
			});
		}
		return archives;
	}

	private SimpleBackup createBackupForFile(File f, SimpleDateFormat sdf) {
		Matcher m = ARCHIVE_NAME_PAT.matcher(f.getName());
		if ( m.matches() ) {
			try {
				Date d = sdf.parse(m.group(2));
				return new SimpleBackup(d, m.group(2), f.length(), true);
			} catch ( ParseException e ) {
				log.error("Error parsing date from archive " + f.getName() + ": " + e.getMessage());
			}
		}
		return null;
	}

	@Override
	public Collection<Backup> getAvailableBackups() {
		File[] archives = getAvailableBackupFiles();
		if ( archives == null ) {
			return Collections.emptyList();
		}
		List<Backup> result = new ArrayList<Backup>(archives.length);
		SimpleDateFormat sdf = new SimpleDateFormat(ARCHIVE_NAME_DATE_FORMAT);
		for ( File f : archives ) {
			SimpleBackup b = createBackupForFile(f, sdf);
			if ( b != null ) {
				result.add(b);
			}
		}
		return result;
	}

	private File markedBackupForRestoreFile() {
		// use default backup dir always, as configuration properties might not be available
		return new File(defaultBackuprDir(), "RESTORE_ON_BOOT");
	}

	private ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper;
	}

	private static final String MARKED_BACKUP_PROP_KEY = "key";
	private static final String MARKED_BACKUP_PROP_PROPS = "props";

	@Override
	public synchronized boolean markBackupForRestore(Backup backup, Map<String, String> props) {
		File markFile = markedBackupForRestoreFile();
		if ( backup == null ) {
			if ( markFile.exists() ) {
				log.info("Clearing marked backup.");
				return markFile.delete();
			}
			return true;
		} else if ( markFile.exists() ) {
			log.warn("Marked backup exists, will not mark again");
			return false;
		} else {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put(MARKED_BACKUP_PROP_KEY, backup.getKey());
			if ( props != null && !props.isEmpty() ) {
				data.put(MARKED_BACKUP_PROP_PROPS, props);
			}
			try {
				objectMapper().writeValue(markFile, data);
				return true;
			} catch ( IOException e ) {
				log.warn("Failed to create restore mark file {}", markFile, e);
			}
			return false;
		}
	}

	@Override
	public synchronized Backup markedBackupForRestore(Map<String, String> props) {
		File markFile = markedBackupForRestoreFile();
		if ( markFile.exists() ) {
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = objectMapper().readValue(markFile, Map.class);
				if ( data == null || !data.containsKey(MARKED_BACKUP_PROP_KEY) ) {
					return null;
				}
				String key = (String) data.get(MARKED_BACKUP_PROP_KEY);
				if ( props != null && data.get(MARKED_BACKUP_PROP_PROPS) instanceof Map ) {
					@SuppressWarnings("unchecked")
					Map<String, String> dataProps = (Map<String, String>) data
							.get(MARKED_BACKUP_PROP_PROPS);
					props.putAll(dataProps);
				}
				return backupForKey(key);
			} catch ( IOException e ) {
				log.warn("Failed to read restore mark file {}", markFile, e);
			}
		}
		return null;
	}

	@Override
	public SettingSpecifierProvider getSettingSpecifierProvider() {
		return this;
	}

	private static class ArchiveFilter implements FilenameFilter {

		final Long nodeId;

		private ArchiveFilter(Long nodeId) {
			super();
			this.nodeId = nodeId;
		}

		@Override
		public boolean accept(File dir, String name) {
			Matcher m = ARCHIVE_NAME_PAT.matcher(name);
			return (m.matches() && (nodeId == null || nodeId.equals(Long.valueOf(m.group(1)))));
		}

	}

	private BackupStatus getStatus() {
		synchronized ( status ) {
			if ( backupDir == null ) {
				return Unconfigured;
			}
			if ( !backupDir.exists() ) {
				if ( !backupDir.mkdirs() ) {
					log.warn("Could not create backup dir {}", backupDir.getAbsolutePath());
					return Unconfigured;
				}
			}
			if ( !backupDir.isDirectory() ) {
				log.error("Configured backup location is not a directory: {}",
						backupDir.getAbsolutePath());
				return Unconfigured;
			}
			return status;
		}
	}

	private void setStatus(BackupStatus newStatus) {
		synchronized ( status ) {
			status = newStatus;
		}
	}

	private BackupStatus setStatusIf(BackupStatus newStatus, BackupStatus ifStatus) {
		synchronized ( status ) {
			if ( status == ifStatus ) {
				status = newStatus;
			}
			return status;
		}
	}

	public File getBackupDir() {
		return backupDir;
	}

	public void setBackupDir(File backupDir) {
		this.backupDir = backupDir;
	}

	public int getAdditionalBackupCount() {
		return additionalBackupCount;
	}

	public void setAdditionalBackupCount(int additionalBackupCount) {
		this.additionalBackupCount = additionalBackupCount;
	}

	public OptionalService<IdentityService> getIdentityService() {
		return identityService;
	}

	public void setIdentityService(OptionalService<IdentityService> identityService) {
		this.identityService = identityService;
	}

}
