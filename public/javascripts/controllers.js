var app = angular.module('connectApp', []);

app.controller('ConnectController', function ($scope, $http) {

//--RESTful Calls--

    $scope.neurons = null;

    $scope.fetchNeurons = function(end) {
        $http.get('positions/' + end)
            .success(function (data) {
                $scope.neurons = data;

                JSONData = $scope.neurons;

                (function() {
                    var data = JSONData.slice();
                    var xPos = function(d) { return d.xPos };
                    var yPos = function(d) { return d.yPos };

                    var x = d3.time.scale()
                        .range([10, 280])
                        .domain(d3.extent(data, yPos));

                    var y = d3.scale.linear()
                        .range([180, 10])
                        .domain(d3.extent(data, xPos));

                    var svg = d3.select("#graph").append("svg:svg")
                        .attr("width", 300)
                        .attr("height", 200);

                    svg.selectAll("circle").data(data).enter()
                        .append("svg:circle")
                        .attr("r", 4)
                        .attr("cx", function(d) { return x(xPos(d)) })
                        .attr("cy", function(d) { return y(yPos(d)) })
                })();

            })
            .error(function (data, status, headers, config) {

            });
    };

});