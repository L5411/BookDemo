package com.l_5411.bookdemo.chapter_2.binder_pool;

import android.os.RemoteException;

/**
 * AIDL ISecurityCcenter 实现
 * Created by L_5411 on 2017/8/5.
 */

public class SecurityCenterImpl extends ISecurityCenter.Stub {

    private static final char SECRET_CODE = '^';

    @Override
    public String encrypt(String content) throws RemoteException {
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= SECRET_CODE;
        }
        return new String(chars);
    }

    @Override
    public String decrypt(String password) throws RemoteException {
        return encrypt(password);
    }
}
