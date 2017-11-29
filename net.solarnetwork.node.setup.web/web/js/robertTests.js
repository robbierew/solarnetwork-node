
//debug to ensure script loads
console.log("robert's graph script loaded");

//maps the current data value with the sourceId
var datamap = {};

//a record of all sourceIds we are graphing
var graphs = [];

//when a new datum comes in this handler gets called
var handler = function handleMessage(msg) {
	
	var datum = JSON.parse(msg.body).data;

	//check that the datum has a reading
	if (datum.voltage != undefined){
		//map the sourceId to the current reading
		datamap[datum.sourceId] = datum.voltage;
		
		//if we have not seen this sourceId before we need to graph it
		if (graphs.indexOf(datum.sourceId)==-1){
			
			graphs.push(datum.sourceId);
			graphinit(datum.sourceId);

		}
	}
	
	
};

//creates a new graph looking for data from the sourceId
function graphinit(source){


    var n = 243,//how many sample points to have on the graph
        duration = 1000,//time for the animation 
        now = new Date(Date.now() -duration),//not sure what the -duration is for

        //prefill the array with the first reading (might change in future)
        data = new Array(n).fill(datamap[source]);
  
    //positional styling for the graph
    var margin = { top: 10, right: 0, bottom: 20, left: 60 },
        width = 960 - margin.right,
        height = 120 - margin.top - margin.bottom;

    //sets the axis scales
    var x = d3.time.scale()
        .domain([now - (n - 2) * duration, now - duration])
        .range([0, width]);

    var y = d3.scale.linear()
        .range([height, 0]);

    //draws the line for the graph (not sure how this code works at this stage)
    var line = d3.svg.line()
        .interpolate("basis")
        .x(function (d, i) { return x(now - (n - 1 - i) * duration); })
        .y(function (d, i) { return y(d); });

    //finds the location on the main page where graphs are to be placed and adds one
    var p = d3.select(".test2").append("p").text(source);
    var svg = p.append("svg")
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

    //causes the animation of the graph to progress once called it will call itself again
    function tick() {
        transition = transition.each(function () {

            // update the domains
            now = new Date();
            x.domain([now - (n - 2) * duration, now - duration]);
            y.domain([d3.min(data), d3.max(data)]);//how to stretch the data leave the 0

            //put a data point on the graph
            data.push(datamap[source]);

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
    
    //start animating the graph
    tick();
}

//subscribe to get datums as they come, when a datum arrives it runs the handler
var topic = SolarNode.WebSocket.topicNameWithWildcardSuffix('/topic/datum/*', null);
SolarNode.WebSocket.subscribeToTopic(topic, handler);
