/*
Copyright 2013 TENEA TECNOLOGÍAS. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:
 
   1. Redistributions of source code must retain the above copyright notice, this list of
      conditions and the following disclaimer.
 
   2. Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY TENEA TECNOLOGÍAS ''AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL TENEA TECNOLOGÍAS OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
The views and conclusions contained in the software and documentation are those of the
authors and should not be interpreted as representing official policies, either expressed
or implied, of TENEA TECNOLOGÍAS.
 */

package com.tenea.filecache;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.content.Context;

public class FileCache {

	private Context context;

	public FileCache(Context context) {
		this.context = context;
	}

	public boolean add(String input, byte[] data) {
		// Make the hash
		String hash = makeHash(input);
		
		// Get the file handle
		File file = file(hash);

		// Check if file exists
		if (exists(file)) {
			return true;
		}

		// Check data
		if (data.length > 0) {
			// Put the data
			return save(file, data);
		} else {
			return false;
		}
	}

	public byte[] get(String input) {
		// Make the hash
		String hash = makeHash(input);
		
		byte[] data = null;

		// Get the file handle
		File file = file(hash);

		// Check if file exists
		if (exists(file)) {
			// Get the data
			data = load(file);
		}

		return data;
	}
	
	public boolean remove(String input) {
		// Make the hash
		String hash = makeHash(input);

		// Get the file handle
		File file = file(hash);

		// Check if file exists
		if (exists(file)) {
			// Get the data
			return file.delete();
		} else {
			return false;
		}
	}

	@SuppressLint("DefaultLocale")
	private String computeHash(byte[] byteArray) {
		String hash = "";

		// Compute hash
		try {
			// Compute md5
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] md5 = digest.digest(byteArray);

			// Convert to String
			for (int i = 0; i < md5.length; i++) {
				hash += Integer.toString((md5[i] & 0xff) + 0x100, 16).substring(1);
			}
		} catch (NoSuchAlgorithmException e) {
		}

		return hash.toLowerCase();
	}

	private String makeHash(String input) {
		// Create a hash from the input
		return computeHash(input.getBytes());
	}
	
	private boolean exists(File file) {
		// Check if the file exists
		return file.exists();
	}

	private boolean save(File file, byte[] data) {
		// Save the data into the file
		try {
			file.createNewFile();
		} catch (IOException e) {
			return false;
		}

		try {
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(data, 0, data.length);
			stream.flush();
			stream.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private byte[] load(File file) {
		byte[] data = null;

		// Load the file from the disk
		FileInputStream fin;

		try {
			fin = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fin);
			DataInputStream dis = new DataInputStream(bis);
			data = toByteArray(dis);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		return data;
	}

	private File file(String hash) {
		// Create the file to be tested
		String filename = hash;

		// Check if the file exists
		return new File(context.getExternalCacheDir(), filename);
	}

	// Support methods

	private static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(in, out);
		return out.toByteArray();
	}

	private static long copy(InputStream from, OutputStream to) throws IOException {
		byte[] buf = new byte[4096];
		long total = 0;
		while (true) {
			int r = from.read(buf);
			if (r == -1) {
				break;
			}
			to.write(buf, 0, r);
			total += r;
		}
		return total;
	}
}
