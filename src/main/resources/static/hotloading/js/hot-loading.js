// 提示框
toastr.options.positionClass = 'toast-top-center';

// 选择class
$("#select").click(function () {
    $('input[id=classFile]').click();
});

// 显示文件全路径
$('input[id=classFile]').change(function() {
    $('#pathCover').val($(this).val());
});

// 发送请求
$("#load").click(function () {
    // 发送文件到后台
    var formData = new FormData();
    if ($('input[id=classFile]').val() == "" || $('input[id=packagePath]').val() == "") {
        toastr.warning("输入必须完整");
        return;
    }

    formData.append('classFile', $('input[id=classFile]')[0].files[0]);
    formData.append('packagePath', $('input[id=packagePath]').val());

    $.ajax({
        type: "post",
        url: "load",
        data: formData,
        processData: false,
        contentType: false,
        success: function(msg) {
            toastr.success("加载成功");
        },
        error: function (error) {
            toastr.error("加载失败！");
        }
    });
});