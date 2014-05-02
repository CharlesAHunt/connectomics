var app = angular.module('connectApp', []);

app.controller('ConnectController', function ($scope, $http) {

//--RESTful Calls--

    $scope.neurons = null;

    $scope.fetchNeurons = function(end) {
        $http.get('positions/' + end)
            .success(function (data) {
                $scope.neurons = data;
            })
            .error(function (data, status, headers, config) {

            });
    };

});