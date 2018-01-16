package com.java.library.groovy

import java.sql.Timestamp
def abc = 'abc'
println  'Hello World!${abc}' // 
println  "Hello World!${abc}" // 引用外部变量 必须使用双引号
println new Timestamp(System.currentTimeMillis())
User user = new User()
user.userId = 1;
user.userName = 'test';
println user
User user2 = new User()
user2.userId = 2;
user2.userName = 'test2';
def alist = [user, user2];
alist.each{item -> println item.userId + ':' + item.userName}

File file = new File('ip.txt')
file.eachLine("GBK") {line, no -> println no + ' ' +  line}

