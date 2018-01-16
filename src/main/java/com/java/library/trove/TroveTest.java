package com.java.library.trove;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class TroveTest {
	
	public static void test() {
		TIntObjectMap<String> map = new TIntObjectHashMap<String>(); 
		map.put(1, "abc");
		System.out.println(map);
	}

	public static void main(String[] args) {
		test();

	}

}
