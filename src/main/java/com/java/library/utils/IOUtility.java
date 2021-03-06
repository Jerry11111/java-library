package com.java.library.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

public class IOUtility {
	
	 private static final int EOF = -1;
	 private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	 
	 // read toByteArray
	 //-----------------------------------------------------------------------
	public static byte[] toByteArray(InputStream input,int size){
		byte[] data;
		try{
			//if has len
			if (size >= 0) {
				data = new byte[size];
				int off = 0;
				while (off < size) {
					int read = input.read (data, off, size - off);
					if (read < 0)
						throw new IOException ("EOF");
					off += read;
				}
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream ();
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				int read=-1;
				while ((read = input.read (buffer, 0, buffer.length))>0) {
					baos.write (buffer, 0, read);
				}
				baos.close ();
				data = baos.toByteArray ();
			}
		}catch(Exception e){
			throw new RuntimeException(e.getMessage(),e);
		}
		return data;
	}
	
	public static byte[] toByteArray(InputStream input, long size){
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
		}
		return toByteArray(input, (int) size);
	}
	
	public static byte[] toByteArray(InputStream input){
		byte[] data;
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream ();
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int read=-1;
			while ((read = input.read (buffer, 0, buffer.length))>0) {
				baos.write (buffer, 0, read);
			}
			baos.close ();
			data = baos.toByteArray ();
		}catch(Exception e){
			throw new RuntimeException(e.getMessage(),e);
		}
		return data;
	}
	
	
	// read InputStream toString
    //-----------------------------------------------------------------------
    public static String toString(InputStream input){
        return toString(input, Charset.defaultCharset().name());
    }
	
    public static String toString(InputStream input, String encoding){
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }
    
    // read URL to InputStream
    //-----------------------------------------------------------------------
    
    public static String toString(URL url){
        return toString(url, Charset.defaultCharset().toString());
    }

    public static String toString(URL url, String encoding) {
        return toString(url, encoding);
    }
	
	
    // copy from InputStream
    //-----------------------------------------------------------------------
    
    public static long copy(InputStream input, OutputStream output){
        return copy(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }
	
    public static long copy(InputStream input, OutputStream output, byte[] buffer){
        long count = 0;
        int n = 0;
        try{
	        while (EOF != (n = input.read(buffer))) {
	            output.write(buffer, 0, n);
	            count += n;
	        }
        }catch(Exception e){
        	throw new RuntimeException(e.getMessage(), e);
        }
        return count;
    }
    
    public static long copy(InputStream input, OutputStream output, long size){
        long count = 0;
        int n = 0;
        int buf = DEFAULT_BUFFER_SIZE;
		if (buf > size) {
			buf = (int) size;
		}
		byte[] buffer = new byte[buf];
        try{
	        while (EOF != (n = input.read(buffer))) {
	            output.write(buffer, 0, n);
	            count += n;
	            if (count == size) {
					break;
				}
	        }
        }catch(Exception e){
        	throw new RuntimeException(e.getMessage(), e);
        }
        return count;
    }
    
    public static void copy(InputStream input, Writer output){
        copy(input, output, Charset.defaultCharset().name());
    }
    
    public static void copy(InputStream input, Writer output, String encoding){
        InputStreamReader in;
		try {
			in = new InputStreamReader(input, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
        copy(in, output);
    }
    
    public static void copy(InputStream input, PrintStream output, String encoding){
        InputStreamReader in;
		try {
			in = new InputStreamReader(input, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
        copy(in, output);
    }
    
    // copy from Reader
    //-----------------------------------------------------------------------
    public static long copy(Reader input, Writer output){
        return copy(input, output, new char[DEFAULT_BUFFER_SIZE]);
    }
    
    public static long copy(Reader input, Writer output, char [] buffer) {
        long count = 0;
        int n = 0;
        try{
	        while (EOF != (n = input.read(buffer))) {
	            output.write(buffer, 0, n);
	            count += n;
	        }
        }catch(Exception e){
        	throw new RuntimeException(e.getMessage(),e);
        }
        return count;
    }
    
    public static void copy(Reader input, PrintStream output) {
    	copy(input, output, DEFAULT_BUFFER_SIZE);
    }
    
    public static void copy(Reader input, PrintStream output, int bufferSize) {
    	BufferedReader bf = new BufferedReader(input, bufferSize);
        String line = null;
        try{
	        while (null != (line = bf.readLine())) {
	        	output.println(line);
	        }
        }catch(Exception e){
        	throw new RuntimeException(e.getMessage(),e);
        }
    }
    
    /// copy file
    public static void copy(InputStream input, File f){
    	FileOutputStream output = null;
		try {
			output = new FileOutputStream(f);
			if( !f.exists() ){
				f.createNewFile();
			}
			copy(input, output);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}finally{
			closeQuietly(output);
		}
    }
    
    /**
     * Start two thread, one read and one write
     */
    public static final void readWrite(final InputStream remoteInput,
			final OutputStream remoteOutput, final InputStream localInput,
			final OutputStream localOutput,final String charset) {
		Thread reader, writer;

		reader = new Thread() {
			@Override
			public void run() {
				int ch;

				try {
					while (!interrupted() && (ch = localInput.read()) != -1) {
						remoteOutput.write(ch);
						remoteOutput.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		writer = new Thread() {
			@Override
			public void run() {
				try {
					IOUtility.copy(remoteInput, (PrintStream)localOutput, charset);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		};

		writer.setPriority(Thread.currentThread().getPriority() + 1);

		writer.start();
		reader.setDaemon(true);
		reader.start();

		try {
			writer.join();
			reader.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    
    // write to OutputStream
    //-----------------------------------------------------------------------
    public static void write(byte[] data, OutputStream output)
            throws IOException {
        if (data != null) {
            output.write(data);
        }
    }
    
    public static void write(char[] data, OutputStream output) {
    	try{
	        if (data != null) {
	            output.write(new String(data).getBytes(Charset.defaultCharset().toString()));
	        }
        }catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
    }
    
    public static void write(char[] data, OutputStream output, String encoding) {
    	try{
	        if (data != null) {
	            output.write(new String(data).getBytes(encoding));
	        }
    	}catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
    }
    
    public static void write(String data, OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset().name());
    }

    public static void write(String data, OutputStream output, String encoding){
        write(data, output, encoding);
    }
    
    // write to Writer
    //-----------------------------------------------------------------------
    
    public static void write(byte[] data, Writer output){
        write(data, output, Charset.defaultCharset());
    }

    public static void write(byte[] data, Writer output, Charset encoding){
    	try{
	        if (data != null) {
	            output.write(new String(data, encoding));
	        }
    	}catch(Exception e){
    		throw new RuntimeException(e.getMessage(), e);
    	}
    }

    public static void write(byte[] data, Writer output, String encoding) {
        write(data, output, encoding);
    }
    
    public static void write(char[] data, Writer output){
    	try{
	        if (data != null) {
	            output.write(data);
	        }
    	}catch(Exception e){
    		throw new RuntimeException(e.getMessage(), e);
    	}
    }
    
    // content equals
    //-----------------------------------------------------------------------
    public static boolean contentEquals(InputStream input1, InputStream input2){
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }
        try{
	        int ch = input1.read();
	        while (EOF != ch) {
	            int ch2 = input2.read();
	            if (ch != ch2) {
	                return false;
	            }
	            ch = input1.read();
	        }
	        
	        int ch2 = input2.read();
	        return ch2 == EOF;
        }catch(Exception e){
        	throw new RuntimeException(e.getMessage(),e);
        }

    }
    
    public static boolean contentEquals(Reader input1, Reader input2){
    	if(input1 instanceof BufferedReader){
    		input1 = new BufferedReader(input1);
    	}
    	if(input1 instanceof BufferedReader){
    		input2 = new BufferedReader(input2);
    	}
    	try{
	        int ch = input1.read();
	        while (EOF != ch) {
	            int ch2 = input2.read();
	            if (ch != ch2) {
	                return false;
	            }
	            ch = input1.read();
	        }
	
	        int ch2 = input2.read();
	        return ch2 == EOF;
    	}catch(Exception e){
    		throw new RuntimeException(e.getMessage(),e);
    	}
    }
    
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }


}
