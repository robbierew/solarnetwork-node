/* ==================================================================
 * BackupManager.java - Mar 27, 2013 9:10:39 AM
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

import java.util.Iterator;
import net.solarnetwork.node.settings.SettingSpecifierProvider;

/**
 * Manager API for node-level backups.
 * 
 * @author matt
 * @version 1.0
 */
public interface BackupManager extends SettingSpecifierProvider {

	/**
	 * Get the active {@link BackupService}.
	 * 
	 * @return the BackupService, or <em>null</em> if none configured
	 */
	BackupService activeBackupService();

	/**
	 * Get a {@link Iterator} of {@link BackupResource} needing to be backed up.
	 * 
	 * @return
	 */
	Iterable<BackupResource> resourcesForBackup();

}
