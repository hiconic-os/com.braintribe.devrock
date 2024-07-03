// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.devenv.processing.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;

public class EclipseLocation {

    private byte[] data;
    private int length;
    private String uri;


    public EclipseLocation(File file) {
    	try {
			RandomAccessFile f = new RandomAccessFile(file, "r");
			byte data[] = new byte[(int)f.length()];
			f.readFully(data);
			init(data);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    }
    
    public EclipseLocation(byte[] data) {
        init(data);
    }

    public String getUri() {
        return uri;
    }

    public byte[] getData() {
        return data;
    }


    private void init(byte[] data) {

        this.data = data;   
        this.length = (data[16] * 256) + data[17];
        this.uri = new String(data,18,length);  
    }


    public void changeUri(String newUri) {

        int newLength = newUri.length();
        byte[] newdata = new byte[data.length + newLength - length];        


        int y = 0;
        int x = 0;

        //header
        while(y < 16) newdata[y++] = data[x++];

        //length
        newdata[16] = (byte) (newLength / 256);
        newdata[17] = (byte) (newLength % 256);

        y += 2;
        x += 2;

        //Uri
        for(int i = 0;i < newLength;i++)
        {
            newdata[y++] = (byte) newUri.charAt(i);
        }
        x += length;

        //footer
        while(y < newdata.length) newdata[y++] = data[x++];

        if(y != newdata.length)
            throw new IndexOutOfBoundsException();

        if(x != data.length)
            throw new IndexOutOfBoundsException();

        init(newdata);
    }
    
    public void write(File file) {
    	try (OutputStream out = new FileOutputStream(file)) {
    		out.write(data);
    	} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    }


}