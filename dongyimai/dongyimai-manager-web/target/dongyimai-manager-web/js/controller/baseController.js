app.controller('baseController', function ($scope) {

    $scope.reloadList = function () {
        // $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    // 4. 定义conf
    // 内部的字段命名必须固定
    $scope.paginationConf = {
        currentPage:1, // 当前页
        totalItems:10, //   总条数
        itemsPerPage:10, // 每页显示的数据数
        perPageOptions:[10,20,30,40,50], // 每页显示数据数的下拉选择框
        onChange: function () { // 当页码改变时自动触发的方法
            $scope.reloadList(); // 页码触发后需要调用java后台进行分页查询
        }
    }


    $scope.selectIds = []; // 记录选择的id
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) { // 选中
            $scope.selectIds.push(id);
        } else { // 取消选中
            // 没有直接删除的方法, 通过切割来实现删除 - 先获取下标, 再进行切割
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);
        }
    }

});