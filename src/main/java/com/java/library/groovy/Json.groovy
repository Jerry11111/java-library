package com.java.library.groovy

import groovy.json.*
class Me {
	 String name
}
def o = new Me( name: 'tim' )
println new JsonBuilder( o ).toPrettyString()


