package com.example.mvvmframe.bean.login

/**
 * @author jingbin
 * @data 2018/5/7
 * @Description
 */

class LoginBean {


    /**
     * data : {"collectIds":[2317,2255,2324],"email":"","icon":"","id":1534,"password":"jingbin54770","type":0,"username":"jingbin"}
     * errorCode : 0
     * errorMsg :
     */

    var data: DataBean? = null
    var errorCode: Int = 0
    var errorMsg: String? = null

    class DataBean {
        /**
         * collectIds : [2317,2255,2324]
         * email :
         * icon :
         * id : 1534
         * password : jingbin54770
         * type : 0
         * username : jingbin
         */

        var email: String? = null
        var icon: String? = null
        var id: Int = 0
        var password: String? = null
        var type: Int = 0
        var username: String? = null
        var collectIds: List<Int>? = null
    }
}
