package com.java.library.groovy

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.*

import com.gargoylesoftware.htmlunit.MockWebConnection.RawResponseData
import com.oracle.webservices.internal.api.databinding.DatabindingModeFeature
import com.sun.javafx.scene.control.TableColumnSortTypeWrapper

import static groovyx.net.http.ContentType.*

println  'Hello World!'

def doRequest() {
	def http = new HTTPBuilder('http://www.baidu.com')
	http.request(GET,TEXT) {
		//设置url相关信息
		uri.path='/'
		uri.query=[a:'1',b:2]
		//设置请求头信息
		headers.'User-Agent' = 'Mozill/5.0'
		//设置成功响应的处理闭包
		response.success= {resp,reader->
			println resp
			println resp.data
			println resp.status
			println resp.statusLine.statusCode
			println resp.headers.'content-length'
			println resp.headers
			//println reader.text
			//System.out << reader
		}
		//根据响应状态码分别指定处理闭包
		response.'404' = { println 'not found' }
		//未根据响应码指定的失败处理闭包
		response.failure = { println "Unexpected failure: ${resp.statusLine}" }
	}
}

// request json/reponse text
def doPost() {
	def http = new HTTPBuilder('http://localhost:8080/mm-payserver/mgr/groovy/postJson')
	http.request( POST, TEXT) {
	  requestContentType = JSON
	  body =  [ status : 'update!' , source : 'httpbuilder' ]
	  response.success = { resp, reader ->
		println resp
		println resp.status
		println reader.text
	  }
	}
}

// request json/reponse json
def doPostJSON() {
	def http = new HTTPBuilder('http://localhost:8080/mm-payserver/mgr/groovy/postJson')
	http.request( POST, JSON) {
		requestContentType = JSON // 指定请求的类型 上面JSON处理响应的类型,将body转化为指定的格式 也可以自动转化为字符串
		body = [ status : 'update!' , source : 'httpbuilder' ]
		response.success = { resp, json ->
			println resp.status
			println json
		}
	}
}

def doGet() {
	def http = new HTTPBuilder('http://www.baidu.com')
	http.get(query:[q:'groovy'],contentType:TEXT){resp, reader->
		println resp.status
		println reader.text
	}
}

def doPost2() {
	def http = new HTTPBuilder('http://localhost:8080/mm-payserver/mgr/groovy/postJson')
	def postBody = [ status : 'update!' , source : 'httpbuilder' ]
	http.post(requestContentType:JSON,body:postBody,contentType:TEXT){resp, reader->
		println resp.status
		println reader.text
	}
}

def doPost3() {
	def http = new HTTPBuilder('http://localhost:8080/mm-payserver/mgr/groovy/postJsons')
	def postBody = [ status : 'update!' , source : 'httpbuilder' ]
	try {
		http.post(requestContentType:JSON,body:postBody,contentType:JSON){resp, reader->
			println resp.status
			println reader
		}
	} catch (Exception e) {
		//e.printStackTrace()
	}
}

doGet();


















