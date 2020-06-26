package com.androidlabz.nfcchipreader

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import java.io.IOException

class NfcChipReader(
    private val mNfcAdapter: NfcAdapter?,
    private val mCallback: Callback
) {
    fun nfcTagReadBuilder(intent: Intent) {
        if (mNfcAdapter != null) {
            if (intent.extras != null) {
                var mTag =
                    intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                mTag = patchTag(mTag)
                mTag?.let { readFromNFC(it, intent) }
            }
        } else {
            mCallback.onNfcDisabled()
        }
    }

    private fun patchTag(oTag: Tag?): Tag? {
        if (oTag == null) {
            return null
        }
        val sTechList = oTag.techList
        val oParcel: Parcel
        val nParcel: Parcel
        oParcel = Parcel.obtain()
        oTag.writeToParcel(oParcel, 0)
        oParcel.setDataPosition(0)
        val len = oParcel.readInt()
        var id: ByteArray? = null
        if (len >= 0) {
            id = ByteArray(len)
            oParcel.readByteArray(id)
        }
        val oTechList = IntArray(oParcel.readInt())
        oParcel.readIntArray(oTechList)
        val oTechExtras = oParcel.createTypedArray(Bundle.CREATOR)
        val serviceHandle = oParcel.readInt()
        val isMock = oParcel.readInt()
        val tagService: IBinder?
        tagService = if (isMock == 0) {
            oParcel.readStrongBinder()
        } else {
            null
        }
        oParcel.recycle()
        var nfca_idx = -1
        var mc_idx = -1
        for (idx in sTechList.indices) {
            if (sTechList[idx] == NfcA::class.java.name) {
                nfca_idx = idx
            } else if (sTechList[idx] == MifareClassic::class.java.name) {
                mc_idx = idx
            }
        }
        if (oTechExtras != null) {
            if (nfca_idx >= 0 && mc_idx >= 0 && oTechExtras[mc_idx] == null) {
                oTechExtras[mc_idx] = oTechExtras[nfca_idx]
            } else {
                return oTag
            }
        }
        nParcel = Parcel.obtain()
        if (id != null) {
            nParcel.writeInt(id.size)
            nParcel.writeByteArray(id)
        }
        nParcel.writeInt(oTechList.size)
        nParcel.writeIntArray(oTechList)
        nParcel.writeTypedArray(oTechExtras, 0)
        nParcel.writeInt(serviceHandle)
        nParcel.writeInt(isMock)
        if (isMock == 0) {
            nParcel.writeStrongBinder(tagService)
        }
        nParcel.setDataPosition(0)
        val nTag = Tag.CREATOR.createFromParcel(nParcel)
        nParcel.recycle()
        return nTag
    }

    private fun readFromNFC(tag: Tag, intent: Intent) {
        if (mNfcAdapter != null) {
            try {
                val ndef = Ndef.get(tag)
                if (ndef != null) {
                    ndef.connect()
                    val ndefMessage = ndef.ndefMessage
                    if (ndefMessage != null) {
                        val messages =
                            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                        if (messages != null) {
                            val ndefMessages =
                                arrayOfNulls<NdefMessage>(messages.size)
                            for (i in messages.indices) {
                                ndefMessages[i] = messages[i] as NdefMessage
                            }
                            val record = ndefMessages[0]!!.records[0]
                            val payload = record.payload
                            val text = String(payload)
                            mCallback.onRecieveTag(text)
                            ndef.close()
                        }
                    } else {
                        mCallback.onReadTagError()
                    }
                } else {
                    val format = NdefFormatable.get(tag)
                    if (format != null) {
                        try {
                            format.connect()
                            var ndefMessage: NdefMessage? = null
                            ndefMessage = ndef!!.ndefMessage
                            if (ndefMessage != null) {
                                val message =
                                    String(ndefMessage.records[0].payload)
                                mCallback.onRecieveTag(message)
                                ndef.close()
                            } else {
                                mCallback.onReadTagError()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        mCallback.onReadTagError()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            mCallback.onNfcDisabled()
        }
    }

    interface Callback {
        fun onRecieveTag(tagContents: String?)
        fun onReadTagError()
        fun onNfcDisabled()
    }

}