/*
 * Copyright (C) 2012 Jimmy Theis. Licensed under the MIT License:
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jetheis.android.makeitrain.billing;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;

import android.util.Base64;
import android.util.Log;

import com.jetheis.android.makeitrain.Constants;

public class GooglePlayBillingSecurity {

    private static final String publicKey64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzuIANnBQodRg521GgHvkg/tTn4dQq+yO5Zzcuk/Qc9oKGLr11a8Wxnd9GijfLnuEQqwc1j+RhbRw6VoT5B5M23IR/dG++pOT5+NR4kCBajQhMr7RuzsUzdC5wt5Iyd+tNM4oVXn82r2LzWHq4ssBQG2/+7mCpVCUZmUUoNx9ipvoMeZ2SiXMHMz8SVX+YeuPPESLZVMZ226pA5uqIlX1YoaElMy77EsjI5esjmwTM68+YIrLdp3YhheFHAk8R2G2SrLczLvDaZvuM7ZqqNuBZRWJLiVzjl5NwLUzm+5+9e7i6+QkA/UhinYFLZfWnDA6LY/w3MvA/B4SYD7Ui7JyDwIDAQAB";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static HashSet<Long> sKnownNonces = new HashSet<Long>();

    public static long generateNonce() {
        long nonce = RANDOM.nextLong();
        sKnownNonces.add(nonce);
        return nonce;
    }

    public static void removeNonce(long nonce) {
        sKnownNonces.remove(nonce);
    }

    public static boolean isNonceKnown(long nonce) {
        return sKnownNonces.contains(nonce);
    }

    public static boolean isCorrectSignature(String signedData, String signature) {
        PublicKey publicKey;

        try {
            byte[] decodedKey = Base64.decode(publicKey64, 0);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            Log.e(Constants.TAG, "NoSuchAlgoritmException: " + e.getLocalizedMessage());
            return false;
        } catch (InvalidKeySpecException e) {
            Log.e(Constants.TAG, "InvalidKeySpecException: " + e.getLocalizedMessage());
            return false;
        }

        Signature sig;

        try {
            sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());

            if (!sig.verify(Base64.decode(signature, 0))) {
                Log.e(Constants.TAG, "Bad Google Play signature! Possible security breach!");
                return false;
            }

            return true;
        } catch (NoSuchAlgorithmException e) {
            Log.e(Constants.TAG, "NoSuchAlgorithmException: " + e.getLocalizedMessage());
            return false;
        } catch (InvalidKeyException e) {
            Log.e(Constants.TAG, "InvalidKeyException: " + e.getLocalizedMessage());
            return false;
        } catch (SignatureException e) {
            Log.e(Constants.TAG, "SignatureException: " + e.getLocalizedMessage());
            return false;
        }
    }
}
