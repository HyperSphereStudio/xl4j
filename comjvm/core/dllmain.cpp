/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "stdafx.h"
#include "core.h"
#include "JvmSupport.h"
#include "internal.h"

volatile long g_lActiveObjects = 0;

HRESULT APIENTRY DllGetClassObject (REFCLSID clsid, REFIID iid, LPVOID *ppv) {
	if (!ppv) return E_POINTER;
	if (clsid == CLSID_JvmSupport) {
		HRESULT hr;
		CJvmSupportFactory *pFactory = NULL;
		try {
			pFactory = CJvmSupportFactory::Instance ();
			hr = pFactory->QueryInterface (iid, ppv);
		} catch (std::bad_alloc) {
			hr = E_OUTOFMEMORY;
		}
		if (pFactory) pFactory->Release ();
		return hr;
	} else {
		return ProxyDllGetClassObject (clsid, iid, ppv);
	}
}

static HRESULT RegisterServer (HKEY hkeyRoot) {
	HRESULT hr;
	HKEY hkeyCLSID;
	LPOLESTR lpsz;
	hr = StringFromCLSID (CLSID_JvmSupport, &lpsz);
	if (FAILED (hr)) return hr;
	TCHAR szKey[MAX_PATH];
	hr = StringCbPrintf (szKey, sizeof (szKey), TEXT ("%s%ws"), TEXT ("SOFTWARE\\Classes\\CLSID\\"), lpsz);
	CoTaskMemFree (lpsz);
	if (FAILED (hr)) return hr;
	DWORD dwError = RegCreateKeyEx (hkeyRoot, szKey, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hkeyCLSID, NULL);
	if (dwError == ERROR_SUCCESS) {
		dwError = RegSetValueEx (hkeyCLSID, NULL, 0, REG_SZ, (LPBYTE)TEXT ("BeerDragon.JvmSupport.1"), 12 * sizeof (TCHAR));
		if (dwError != ERROR_SUCCESS) hr = HRESULT_FROM_WIN32 (dwError);
		HKEY hkeyServer;
		dwError = RegCreateKeyEx (hkeyCLSID, TEXT ("InprocServer32"), 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hkeyServer, NULL);
		if (dwError == ERROR_SUCCESS) {
			HMODULE hModule;
			if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)RegisterServer, &hModule)) {
				TCHAR szFilename[MAX_PATH + 1];
				if (GetModuleFileName (hModule, szFilename, sizeof (szFilename) / sizeof (TCHAR)) <= MAX_PATH) {
					size_t cch = _tcslen (szFilename);
					dwError = RegSetValueEx (hkeyServer, NULL, 0, REG_SZ, (LPBYTE)szFilename, ((DWORD)cch + 1) * sizeof (TCHAR));
					if (dwError != ERROR_SUCCESS) hr = HRESULT_FROM_WIN32 (dwError);
				}
				FreeLibrary (hModule);
			}
			dwError = RegSetValueEx (hkeyServer, TEXT ("ThreadingModel"), 0, REG_SZ, (LPBYTE)TEXT ("Both"), 5 * sizeof (TCHAR));
			if (dwError != ERROR_SUCCESS) hr = HRESULT_FROM_WIN32 (dwError);
			RegCloseKey (hkeyServer);
		} else {
			hr = HRESULT_FROM_WIN32 (hr);
		}
		RegCloseKey (hkeyCLSID);
	} else {
		hr = HRESULT_FROM_WIN32 (dwError);
	}
	return hr;
}

static HRESULT UnregisterServer (HKEY hkeyRoot) {
	HRESULT hr;
	LPOLESTR lpsz;
	hr = StringFromCLSID (CLSID_JvmSupport, &lpsz);
	if (FAILED (hr)) return hr;
	TCHAR szKey[MAX_PATH];
	hr = StringCbPrintf (szKey, sizeof (szKey), TEXT ("%s%ws"), TEXT ("SOFTWARE\\Classes\\CLSID\\"), lpsz);
	CoTaskMemFree (lpsz);
	if (FAILED (hr)) return hr;
	DWORD dwError = RegDeleteTree (hkeyRoot, szKey);
	return HRESULT_FROM_WIN32 (dwError);
}

HRESULT APIENTRY DllRegisterServer () {
	HRESULT hr;
	hr = RegisterServer (HKEY_LOCAL_MACHINE);
	if (FAILED (hr)) {
		hr = RegisterServer (HKEY_CURRENT_USER);
	} else {
		hr = ProxyDllRegisterServer ();
	}
	return hr;
}

HRESULT APIENTRY DllUnregisterServer () {
	UnregisterServer (HKEY_CURRENT_USER);
	UnregisterServer (HKEY_LOCAL_MACHINE);
	ProxyDllUnregisterServer ();
	return S_OK;
}

HRESULT APIENTRY DllCanUnloadNow () {
	if (g_lActiveObjects) {
		return S_FALSE;
	} else {
		return ProxyDllCanUnloadNow ();
	}
}

BOOL APIENTRY DllMain (
	HMODULE hModule,
	DWORD  dwReason,
	LPVOID lpReserved
	) {
	return ProxyDllMain (hModule, dwReason, lpReserved);
}
