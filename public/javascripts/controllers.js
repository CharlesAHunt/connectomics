var app = angular.module('connectApp', []);

app.controller('ConnectController', function ($scope, $http) {

//--RESTful Calls--

    $scope.neurons = null;

    $scope.fetchNeurons = function(numberofNeuronsToGet) {
        $http.get('positions/' + numberofNeuronsToGet)
            .success(function (data) {
                $scope.neurons = data;

                JSONData = $scope.neurons;

                (function() {
                    var data = JSONData.slice();
                    var xPos = function(d) { return d.xPos };
                    var yPos = function(d) { return d.yPos };

                    var x = d3.time.scale()
                        .range([10, 580])
                        .domain(d3.extent(data, yPos));

                    var y = d3.scale.linear()
                        .range([380, 10])
                        .domain(d3.extent(data, xPos));

                    var svg = d3.select("#graph").append("svg")
                        .attr("width", 600)
                        .attr("height", 400);

                    var xAxis = d3.svg.axis()
                        .scale(x)
                        .orient("bottom");

                    var yAxis = d3.svg.axis()
                        .scale(y)
                        .orient("left");

                    svg.append("g")
                        .attr("class", "x axis")
                        .attr("transform", "translate(0," + 400 + ")")
                        .call(xAxis)
                        .append("text")
                        .attr("class", "label")
                        .attr("x", 600)
                        .attr("y", -6)
                        .style("text-anchor", "end")
                        .text("X Pos");

                    svg.append("g")
                        .attr("class", "y axis")
                        .call(yAxis)
                        .append("text")
                        .attr("class", "label")
                        .attr("transform", "rotate(-90)")
                        .attr("y", 7)
                        .attr("dy", ".1em")
                        .style("text-anchor", "end")
                        .text("Y Pos");

                    svg.selectAll("circle").data(data).enter()
                        .append("circle")
                        .attr("r", 3)
                        .attr("cx", function(d) { return x(xPos(d)) })
                        .attr("cy", function(d) { return y(yPos(d)) })
                })();

            })
            .error(function (data, status, headers, config) {

            });
    };

    $scope.getRegression = function() {
        $http.get('regression/')
            .success(function (data) {
                $scope.coeffs = data;

                JSONData = $scope.coeffs;

                (function() {
                    var data = JSONData.slice();

                    var margin = {top: 20, right: 20, bottom: 30, left: 50},
                        width = 960 - margin.left - margin.right,
                        height = 500 - margin.top - margin.bottom;

                    var parseDate = d3.time.format("%d-%b-%y").parse;

                    var x = d3.time.scale()
                        .range([0, width]);

                    var y = d3.scale.linear()
                        .range([height, 0]);

                    var xAxis = d3.svg.axis()
                        .scale(x)
                        .orient("bottom");

                    var yAxis = d3.svg.axis()
                        .scale(y)
                        .orient("left");

                    var line = d3.svg.line()
                        .x(function(d) { return x(d.date); })
                        .y(function(d) { return y(d.close); });

                    var svg = d3.select("body").append("svg")
                        .attr("width", width + margin.left + margin.right)
                        .attr("height", height + margin.top + margin.bottom)
                        .append("g")
                        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                    d3.tsv("data.tsv", function(error, data) {
                        data.forEach(function(d) {
                            d.date = parseDate(d.date);
                            d.close = +d.close;
                        });

                        x.domain(d3.extent(data, function(d) { return d.date; }));
                        y.domain(d3.extent(data, function(d) { return d.close; }));

                        // Derive a linear regression
                        var lin = ss.linear_regression().data(data.map(function(d) {
                            return [+d.date, d.close];
                        })).line();

                        // Create a line based on the beginning and endpoints of the range
                        var lindata = x.domain().map(function(x) {
                            return {
                                date: new Date(x),
                                close: lin(+x)
                            };
                        });

                        svg.append("g")
                            .attr("class", "x axis")
                            .attr("transform", "translate(0," + height + ")")
                            .call(xAxis);

                        svg.append("g")
                            .attr("class", "y axis")
                            .call(yAxis)
                            .append("text")
                            .attr("transform", "rotate(-90)")
                            .attr("y", 6)
                            .attr("dy", ".71em")
                            .style("text-anchor", "end")
                            .text("Price ($)");

                        svg.append("path")
                            .datum(data)
                            .attr("class", "line")
                            .attr("d", line);

                        svg.append("path")
                            .datum(lindata)
                            .attr("class", "reg")
                            .attr("d", line);
                    });


                })();

            })
            .error(function (data, status, headers, config) {

            });
    };

});
