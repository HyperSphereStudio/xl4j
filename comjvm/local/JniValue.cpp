#include "stdafx.h"
#include "JniSequence.h"
#include "Internal.h"

void CJniValue::free () {
	switch (type) {
	case t_BSTR:
		/******** THIS REALLY NEEDS FIXING! ********/
		//SysFreeString (v._BSTR);
		break;
	}
}

void CJniValue::put_variant (const VARIANT *pvValue) {
	switch (pvValue->vt) {
	case VT_I4:
		put_jint (pvValue->intVal);
		break;
	case VT_BOOL:
		put_jboolean (pvValue->boolVal == VARIANT_FALSE ? false : true);
		break;
	case VT_BSTR:
		put_BSTR (pvValue->bstrVal); // I'm assuming the caller thinks it's up to us to free it?
		break;
	case VT_I1:
		put_jbyte (pvValue->bVal);
		break;
	case VT_I2:
		put_jshort (pvValue->iVal);
		break;
	case VT_I8:
		put_jlong (pvValue->llVal);
		break;
	case VT_R4:
		put_jfloat (pvValue->fltVal);
		break;
	case VT_R8:
		put_jdouble (pvValue->dblVal);
		break;
	case VT_UI8:
		put_HANDLE (pvValue->ullVal);
		break;
	case VT_SAFEARRAY | VT_UI1: {
		SAFEARRAY *safeArray = pvValue->parray;
		if (safeArray->cDims != 1) {
			assert (0);
		}
		put_jbyteBuffer ((jbyte *)safeArray->pvData, safeArray->cbElements);
	} break;
	default:
		assert (0);
		break;
	}
}

void CJniValue::get_variant (VARIANT *pvValue) const {
	switch (type) {
	case t_jint:
		pvValue->vt = VT_I4;
		pvValue->intVal = v._jvalue.i;
		break;
	case t_jsize:
		pvValue->vt = VT_I4;
		pvValue->intVal = v._jvalue.i;
		break;
	case t_jstring:  // TODO
		pvValue->vt = VT_BSTR;
		pvValue->bstrVal = SysAllocString ((OLECHAR *)v._jvalue.l);
		_com_raise_error (E_NOTIMPL);
		break;
	case t_jboolean:
		pvValue->vt = VT_BOOL;
		pvValue->boolVal = (v._jvalue.z == JNI_TRUE) ? VARIANT_TRUE : VARIANT_FALSE;
		break;
	case t_jbyte:
		pvValue->vt = VT_I1;
		pvValue->bVal = v._jvalue.b;
		break;
	case t_jchar:
		pvValue->vt = VT_UI2; // jchar as unsigned short, effectively.
		pvValue->uiVal = v._jvalue.c;
		break;
	case t_jshort:
		pvValue->vt = VT_I2; // signed short.
		pvValue->iVal = v._jvalue.s;
		break;
	case t_jlong:
		pvValue->vt = VT_I8;
		pvValue->llVal = v._jvalue.j;
		break;
	case t_jfloat:
		pvValue->vt = VT_R4;
		pvValue->fltVal = v._jvalue.f;
		break;
	case t_jdouble:
		pvValue->vt = VT_R8;
		pvValue->dblVal = v._jvalue.d;
		break;
	case t_jbyteBuffer: {
		SAFEARRAYBOUND bounds[1];
		bounds[0].cElements = v._jbyteBuffer._jsize;
		bounds[0].lLbound = 0;
		pvValue->vt = VT_SAFEARRAY | VT_UI1;
		pvValue->parray = SafeArrayCreate (VT_UI1, 1, bounds);
		void *pArrayData = NULL;
		SafeArrayAccessData (pvValue->parray, &pArrayData);
		memcpy (pArrayData, v._jbyteBuffer._pjbyte, v._jbyteBuffer._jsize);
		SafeArrayUnaccessData (pvValue->parray);
	} break;
	case t_jclass:
	case t_jobject:
	case t_jmethodID:
	case t_jfieldID:
	case t_jobjectRefType:
	case t_jthrowable:
	case t_jobjectArray:
	case t_jbooleanArray:
	case t_jbyteArray:
	case t_jcharArray:
	case t_jshortArray:
	case t_jintArray:
	case t_jlongArray:
	case t_jfloatArray:
	case t_jdoubleArray:
	case t_jweak:
		pvValue->vt = VT_UI8;
		pvValue->ullVal = (ULONGLONG)v._jvalue.l; // ick
		break;
	case t_pjchar:
		pvValue->vt = VT_BSTR;
		pvValue->bstrVal = SysAllocString ((const OLECHAR*)v._pjchar);
		if (!pvValue->bstrVal) {
			pvValue->vt = VT_NULL;
			_com_raise_error (E_OUTOFMEMORY);
		}
		break;
	default:
		_com_raise_error (E_INVALIDARG);
		break;
	}
}

void CJniValue::get_jvalue (jvalue *pValue) const {
	switch (type) {
		// these cases are not types you can pass to a Java method.
	case t_jmethodID:
	case t_jfieldID:
	case t_jobjectRefType:
	case t_jbyteBuffer:
	case t_BSTR:
	case t_pjchar:
		_com_raise_error (E_INVALIDARG); // needs to have been allocated already on the java side
		break;
		// these are all types you can pass to a Java method. jsize is a bit marginal.
	case t_jsize:
	case t_jint:
	case t_jstring:
	case t_jboolean:
	case t_jchar:
	case t_jshort:
	case t_jlong:
	case t_jfloat:
	case t_jdouble:
	case t_jclass:
	case t_jobject:
	case t_jthrowable:
	case t_jobjectArray:
	case t_jbooleanArray:
	case t_jbyteArray:
	case t_jcharArray:
	case t_jshortArray:
	case t_jintArray:
	case t_jlongArray:
	case t_jfloatArray:
	case t_jdoubleArray:
	case t_jweak:
		*pValue = v._jvalue;
		break;
	default:
		assert (0);
		break;
	}
}


void CJniValue::copy_into (CJniValue &value) const {
	switch (type) {
	case t_BSTR:
		value.put_BSTR (v._BSTR);
		break;
	default:
		value.reset (type);
		value.v = v;
		break;
	}
}

CJniValue::CJniValue (BSTR bstr)
	: type (t_BSTR) {
	v._BSTR = SysAllocStringLen (bstr, SysStringLen (bstr));
	if (!v._BSTR) throw std::bad_alloc ();
}

void CJniValue::put_BSTR (BSTR bstr) {
	BSTR bstrCopy = SysAllocStringLen (bstr, SysStringLen (bstr));
	if (bstrCopy) {
		reset (t_BSTR);
		v._BSTR = bstrCopy;
	}
	else {
		reset (t_nothing);
	}
}

void CJniValue::put_HANDLE (ULONGLONG handle) {
	if (handle) {
		reset (t_HANDLE);
		v._HANDLE = handle;
	}
	else {
		reset (t_nothing);
	}
}

jchar *CJniValue::get_pjchar () const {
	switch (type) {
	case t_BSTR:
		return (jchar*)v._bstr->pcwstr ();
	case t_pjchar:
		return v._pjchar;
	}
	assert (0);
	return 0;
}

char *CJniValue::get_pchar () const {
	switch (type) {
		case t_BSTR:
			return (char*)v._bstr->pcstr ();
		case t_pchar:
			return v._pchar;
	}
	_com_raise_error (E_INVALIDARG);
}

jobjectRefType CJniValue::get_jobjectRefType_t () const {
	switch (type) {
	case t_jobjectRefType:
		return v._jobjectRefType;
	case t_HANDLE:
		return (jobjectRefType)v._HANDLE;
	}
	_com_raise_error (E_INVALIDARG);
}

jstring CJniValue::get_jstring () const {
	switch (type) {
	case t_jstring:
		return (jstring)v._jvalue.l;
	}
	_com_raise_error (E_INVALIDARG);
}


jint CJniValue::get_jint () const {
	switch (type) {
	case t_jint:
		return v._jvalue.i;
	case t_jsize:
		return v._jvalue.i;
	}
	assert (0);
	return v._jvalue.i;
}

jint CJniValue::get_jsize () const {
	switch (type) {
	case t_jint:
		return v._jvalue.i;
	case t_jsize:
		return v._jvalue.i;
	}
	assert (0);
	return v._jvalue.i;
}

HRESULT CJniValue::load (std::vector<CJniValue> &aValue) {
	try {
		aValue.push_back (*this);
		type = t_nothing;
		return S_OK;
	}
	catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

CJniValue::CJniValue (jbyte *buffer, jsize size) : type (t_jbyteBuffer) {
	v._jbyteBuffer._pjbyte = buffer;
	v._jbyteBuffer._jsize = size;
}

void CJniValue::put_jbyteBuffer (jbyte *buffer, jsize size) {
	reset (t_jbyteBuffer);
	v._jbyteBuffer._pjbyte = buffer;
	v._jbyteBuffer._jsize = size;
}

jbyte *CJniValue::get_jbyteBuffer () const {
	if (type == t_jbyteBuffer) {
		return v._jbyteBuffer._pjbyte;
	}
	_com_raise_error (E_INVALIDARG);
}

jsize CJniValue::get_jbyteBufferSize () const {
	if (type == t_jbyteBuffer) {
		return v._jbyteBuffer._jsize;
	}
	_com_raise_error (E_INVALIDARG);
}