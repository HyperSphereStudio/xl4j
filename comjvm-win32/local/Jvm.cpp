/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "CScan.h"
#include "CCall.h"
#include "CCollect.h"
#include "Jvm.h"

#include "Internal.h"

#include "core/AbstractJvm.cpp"


CJvm::CJvm (IJvmTemplate *pTemplate, const GUID *pguid, DWORD dwJvm)
: CAbstractJvm (pTemplate, pguid), m_dwJvm (dwJvm) {
	IncrementActiveObjectCount ();
}

CJvm::~CJvm () {
	DecrementActiveObjectCount ();
}

/// <summary>Schedules the callback on one of the JVM bound threads.</summary>
///
/// <para>If there are no idle threads, one is spawned and attached to the JVM.</para>
///
/// <param name="pfnCallback">Callback function</param>
/// <param name="lpData">Callback function user data</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvm::Execute (JNICallbackProc pfnCallback, LPVOID lpData) {
	return JNICallback (m_dwJvm, pfnCallback, lpData);
}

/// <summary>Schedules the callback on one of the JVM bound async threads.</summary>
///
/// <para>If there are no idle threads, one is spawned and attached to the JVM.</para>
///
/// <param name="pfnCallback">Callback function</param>
/// <param name="lpData">Callback function user data</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvm::ExecuteAsync(JNICallbackProc pfnCallback, LPVOID lpData) {
	return JNICallbackAsync(m_dwJvm, pfnCallback, lpData);
}

HRESULT STDMETHODCALLTYPE CJvm::FlushAsyncThreads () {
	return JNIFlushAsyncThreads(m_dwJvm);
}

HRESULT STDMETHODCALLTYPE CJvm::CreateScan (
	/* [retval][out] */ IScan **ppScan) {
	if (!ppScan) return E_POINTER;
	try {
		*ppScan = reinterpret_cast<IScan *> (new CScan (this));
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

HRESULT STDMETHODCALLTYPE CJvm::CreateCall (
	/* [retval][out] */ ICall **ppCall) {
	if (!ppCall) return E_POINTER;
	try {
		*ppCall = reinterpret_cast<ICall *> (new CCall (this));
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

HRESULT STDMETHODCALLTYPE CJvm::CreateCollect (
	/* [retval][out] */ ICollect **ppCollect) {
	if (!ppCollect) return E_POINTER;
	try {
		*ppCollect = reinterpret_cast<ICollect *> (new CCollect (this));
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}