console.log("script is running");
var testnum = 0;
var initialised = false;
var handler = function handleMessage(msg) {
	
	var datum = JSON.parse(msg.body).data;
	testnum=datum.voltage;
	if (!initialised){
		graphinit();
		initialised = true;
	}
	console.log("message handled");
	console.log(datum.apparentPower);
};

function graphinit(){


    var random = d3.random.normal(0, .5);

    var n = 243,//how many sample points to have on the graph
        duration = 1000,//time for each sample (will remove when I set up entry)
        now = new Date(Date.now() - duration),//not sure why -duration is there?

        //best guess create an array of n elements and map them all to zero initaly
        //d3.range(n).map(random);

        //this seems clearer
        data = new Array(n).fill(testnum);

    var margin = { top: 10, right: 0, bottom: 20, left: 60 },
        width = 960 - margin.right,
        height = 120 - margin.top - margin.bottom;

    //sets the axis scales
    var x = d3.time.scale()
        .domain([now - (n - 2) * duration, now - duration])
        .range([0, width]);

    var y = d3.scale.linear()
        .range([height, 0]);

    var line = d3.svg.line()
        .interpolate("basis")
        .x(function (d, i) { return x(now - (n - 1 - i) * duration); })
        .y(function (d, i) { return y(d); });

    var svg = d3.select(".test2").append("p").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .style("margin-left", margin.left + "px")
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("defs").append("clipPath")
        .attr("id", "clip")
        .append("rect")
        .attr("width", width)
        .attr("height", height);

    var axis = svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(x.axis = d3.svg.axis().scale(x).orient("bottom"));

    svg.append("g")
        .attr("class", "yaxis")
        .call(d3.svg.axis().scale(y).ticks(5).orient("left"));
    var path = svg.append("g")
        .attr("clip-path", "url(#clip)")
        .append("path")
        .datum(data)
        .attr("class", "line");

    var transition = d3.select({}).transition()
        .duration(duration)
        .ease("linear");


    function tick() {
        transition = transition.each(function () {

            // update the domains
            now = new Date();
            x.domain([now - (n - 2) * duration, now - duration]);
            y.domain([d3.min(data), d3.max(data)]);//how to stretch the data leave the 0

            //put a data point on the graph
            data.push(testnum);

            // redraw the line
            svg.select(".line")
                .attr("d", line)
                .attr("transform", null);

            // slide the x-axis left
            axis.call(x.axis);
                
            svg.select("g .yaxis")
                .call(d3.svg.axis().scale(y).ticks(5).orient("left"));
            //y.axis.ticks(10);
            // slide the line left
            path.transition()
                .attr("transform", "translate(" + x(now - (n - 1) * duration) + ")");

            // pop the old data point off the front
            data.shift();

        }).transition().each("start", function () { tick() });
    };
    tick();
}
var topic = SolarNode.WebSocket.topicNameWithWildcardSuffix('/topic/datum/*', null);

//d3.select("body").selectAll.text("new text");
SolarNode.WebSocket.subscribeToTopic(topic, handler);
