/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "Excel.h"
#include "ExcelUtils.h"

/**
 * Print out an XLOPER12 for debugging purposes.
 * @param pXLOper  a pointer to the XLOPER12 union to be displayed
 */
void ExcelUtils::PrintXLOPER (XLOPER12 *pXLOper) {
	switch (pXLOper->xltype & 0xfff) {
	case xltypeStr: {
		size_t sz = pXLOper->val.str[0]; // the first 16-bit word is the length in chars (not inclusing any zero terminator)
		wchar_t *zeroTerminated = (wchar_t *)malloc ((sz + 1) * sizeof (wchar_t)); // + 1 for zero terminator
		wcsncpy_s (zeroTerminated, sz + 1, (const wchar_t *)pXLOper->val.str + 1, sz); // +1 to ptr to skip length 16 bit word.
		zeroTerminated[sz] = '\0'; // add a NULL terminator
		LOGTRACE ("XLOPER12: xltypeStr: %s", zeroTerminated);
		free (zeroTerminated);
	} break;
	case xltypeNum: {
		LOGTRACE ("XLOPER12: xltypeNum: %f", pXLOper->val.num);
	} break;
	case xltypeNil: {
		LOGTRACE ("XLOPER12: xltypeNil");
	} break;
	case xltypeRef: {
		LOGTRACE ("XLOPER12: xltypeRef: sheetId=%d", pXLOper->val.mref.idSheet);
		if (pXLOper->val.mref.lpmref == NULL) {
			LOGTRACE ("  lpmref = NULL");
			break;
		}
		for (int i = 0; i < pXLOper->val.mref.lpmref->count; i++) {
			LOGTRACE ("  rwFirst=%d,rwLast=%d,colFirst=%d,colLast=%d",
				pXLOper->val.mref.lpmref->reftbl[i].rwFirst,
				pXLOper->val.mref.lpmref->reftbl[i].rwLast,
				pXLOper->val.mref.lpmref->reftbl[i].colFirst,
				pXLOper->val.mref.lpmref->reftbl[i].colLast);
		}
	} break;
	case xltypeMissing: {
		LOGTRACE ("XLOPER12: xltypeMissing");
	} break;
	case xltypeSRef: {
		LOGTRACE ("XLOPER12: cltypeSRef: rwFirst=%d,rwLast=%d,colFirst=%d,colLast=%d",
			pXLOper->val.sref.ref.rwFirst,
			pXLOper->val.sref.ref.rwLast,
			pXLOper->val.sref.ref.colFirst,
			pXLOper->val.sref.ref.colLast);
	} break;
	case xltypeInt: {
		LOGTRACE ("XLOPER12: xltypeInt: %d", pXLOper->val.w);
	} break;
	case xltypeErr: {
		LOGTRACE ("XLOPER12: xltypeErr: %d", pXLOper->val.err);
	} break;
	case xltypeBool: {
		if (pXLOper->val.xbool == FALSE) {
			LOGTRACE ("XLOPER12: xltypeBool: FALSE");
		} else {
			LOGTRACE ("XLOPER12: xltypeBool: TRUE");
		}
	} break;
	case xltypeBigData: {
		LOGTRACE ("XLOPER12: xltypeBigData");
	} break;
	case xltypeMulti: {
		RW cRows = pXLOper->val.array.rows;
		COL cCols = pXLOper->val.array.columns;
		LOGTRACE ("XLOPER12: xltypeMulti: cols=%d, rows=%d", cCols, cRows);
		XLOPER12 *pXLOPER = pXLOper->val.array.lparray;
		for (RW j = 0; j < cRows; j++) {
			for (COL i = 0; i < cCols; i++) {
				LOGTRACE("Element col=%d, row=%d is:", i, j);
				PrintXLOPER (pXLOPER++);
			}
		}
	} break;
	default: {
		LOGTRACE ("XLOPER12: Unrecognised XLOPER12 type %d", pXLOper->xltype);
	}

	}
}

/**
 * Schedule a command for future execution by Excel.  This will take place in the main Excel thread on
 * or after the number of seconds into the future passed as the second argument.
 * @param wsCommandName  a pointer to a null-terminated wide C-string containing the name of the command to invoke
 * @param dbSeconds  the number of seconds into the future after which to invoke the named command
 */
void ExcelUtils::ScheduleCommand (wchar_t *wsCommandName, double dbSeconds) {
	XLOPER12 now;
	Excel12f (xlfNow, &now, 0);
	now.val.num += 2. / (3600. * 24.);
	XLOPER12 retVal;
	Excel12f (xlcOnTime, &retVal, 2, &now, TempStr12 (wsCommandName));
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&now);
}

/**
 * Register a command.  Commands have no arguments or return type so only a name is required.  The name
 * passed is used for both the export name (the name of the C function) and the command name as used when
 * referring to the function to Excel.
 * @param wsCommandName  a const pointer to a null-terminated wide C-string containing the command name
 * @returns integer return code from xlfRegister
 */
int ExcelUtils::RegisterCommand (const wchar_t *wsCommandName) {
	XLOPER12 xDLL;
	Excel12f (xlGetName, &xDLL, 0);
	FreeAllTempMemory ();
	XLOPER12 retVal = {};
	LPXLOPER12 exportName = TempStr12 (wsCommandName);
	LPXLOPER12 returnType = TempStr12 (TEXT ("J"));
	LPXLOPER12 commandName = TempStr12 (wsCommandName);
	LPXLOPER12 args = TempMissing12 ();
	LPXLOPER12 functionType = TempInt12 (2);
	LOGTRACE("Registering command: %s", wsCommandName);
	//LOGTRACE ("xDLL = %p, exportName = %p, returnType = %p, commandName = %p, args = %p, functionType = %p", &xDLL, exportName, returnType, commandName, args, functionType);

	int ret = Excel12f (
		xlfRegister, &retVal, 6, &xDLL,
		exportName, // export name
		returnType, // return type, always J for commands
		commandName, // command name
		args, // args
		functionType // function type 2 = Command
		);
	//ExcelUtils::PrintExcel12Error (ret);
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&xDLL);
	if (ret == xlretSuccess) {
		if (retVal.xltype == xltypeInt) {
			return retVal.val.w;
		} else if (retVal.xltype == xltypeErr) {
			LOGERROR ("Error registering command %s, xltypeErr value was %d", wsCommandName, retVal.val.err);
			return 0;
		} else if (retVal.xltype == xltypeNum) {
			return static_cast<int>(retVal.val.num);
		} else {
			LOGERROR ("LOGIC ERROR: Unexpected return value registering command %s, returned value was:", wsCommandName);
			PrintXLOPER (&retVal);
			return 0;
		}
	} else {
		LOGERROR ("Registration failed");
		return 1;
	}
}

/**
 * Unregister a user-defined function or command with Excel.  This both calls xlfUnregister
 * and also deletes the name associated with the function using xlfSetName.
 * @param szFunctionName  const pointer to a null terminated C-string containing the name of the function to unregister
 * @param iRegisterId  the ID of the function, as returned during registration
 * @returns result code, E_FAIL or S_OK
 */
HRESULT ExcelUtils::UnregisterFunction (const TCHAR *szFunctionName, int iRegisterId) {
	XLOPER12 result;
	Excel12f (xlfUnregister, &result, 1, TempInt12 (iRegisterId));
	if (result.xltype == xltypeErr) {
		LOGERROR ("xlfUnregister on %s returned argument was invalid: xlErr code %d", szFunctionName, result.val.err);
		return E_FAIL;
	} else if (result.xltype == xltypeBool) {
		if (result.val.xbool) {
			LOGTRACE ("Sucessfully unregisterd function %s", szFunctionName);
		} else {
			LOGERROR ("Could not unregister function %s", szFunctionName);
		}
	}
	Excel12f (xlfSetName, &result, 2, TempStr12 (szFunctionName), TempMissing12 ());
	if (result.xltype == xltypeErr) {
		LOGERROR ("xlfSetName on %s returned argument was invalid: xlErr code %d", szFunctionName, result.val.err);
		return E_FAIL;
	} else if (result.xltype == xltypeBool) {
		if (result.val.xbool) {
			LOGTRACE ("Sucessfully unset name %s", szFunctionName);
		} else {
			LOGERROR ("Could not unset name %s", szFunctionName);
			return E_FAIL;
		}
	}
	return S_OK;// should really make this a bit more subtle...
}

// Create a place to store Microsoft Excel's WndProc address
WNDPROC ExcelUtils::g_lpfnExcelWndProc = NULL;

/**
 * ExcelCursorProc
 * When a modal dialog box is displayed over Microsoft Excel's window, the
 * cursor is a busy cursor over Microsoft Excel's window. This WndProc traps
 * WM_SETCURSORs and changes the cursor back to a normal arrow.
 * @param hWndDlg  the HWND Window
 * @param message  the message to respond to
 * @param wParam  argument passed by Windows
 * @param lParam  argument passed by Windows
 * @returns LRESULT, 0 if message handled, otherwise the result of the
 *          default WndProc
 */
LRESULT CALLBACK ExcelUtils::ExcelCursorProc (HWND hwnd,
	UINT wMsg,
	WPARAM wParam,
	LPARAM lParam) {

	// This block checks to see if the message that was passed in is a
	// WM_SETCURSOR message. If so, the cursor is set to an arrow; if not,
	// the default WndProc is called.
	if (wMsg == WM_SETCURSOR) {
		SetCursor (LoadCursor (NULL, IDC_ARROW));
		return 0L;
	} else {
		return CallWindowProc (g_lpfnExcelWndProc, hwnd, wMsg, wParam, lParam);
	}
}

/**
 * HookExcelWindow installs ExcelCursorProc so that it is called before Microsoft 
 * Excel's main WndProc.  This block obtains the address of Microsoft Excel's WndProc 
 * through the use of GetWindowLongPtr(). It stores this value in a global that can 
 * be used to call the default WndProc and also to restore it. Finally, it replaces 
 * this address with the address of ExcelCursorProc using SetWindowLongPtr().
 * @param hWndExcel handle to Microsoft Excel's hWnd
 */
void ExcelUtils::HookExcelWindow (HWND hWndExcel) {
    g_lpfnExcelWndProc = (WNDPROC)GetWindowLongPtr (hWndExcel, GWLP_WNDPROC);
	SetWindowLongPtr (hWndExcel, GWLP_WNDPROC, (LONG_PTR)(FARPROC)ExcelCursorProc);
}

/**
 * UnhookExcelWindow removes the ExcelCursorProc that was called before 
 * Microsoft Excel's main WndProc.  This function restores Microsoft Excel's default 
 * WndProc using SetWindowLongPtr to restore the address that was saved into
 * g_lpfnExcelWndProc by HookExcelWindow(). It then sets g_lpfnExcelWndProc to NULL.
 * @param hWndExcel a handle to Microsoft Excel's hWnd
 */
void ExcelUtils::UnhookExcelWindow(HWND hWndExcel) {
	SetWindowLongPtr (hWndExcel, GWLP_WNDPROC, (LONG_PTR)g_lpfnExcelWndProc);
	g_lpfnExcelWndProc = NULL;
}

/**
 * Display a message box with a warning icon with the provided message.  This will block
 * execution until the user clicks OK.
 * @param szWarningMessage a pointer to a null-terminated wide C-string containing 
 *                         the message to display
 */
void ExcelUtils::WarningMessageBox(wchar_t *szWarningMessage) {
	if (g_pJvmEnv) {
		g_pJvmEnv->HideSplash();
	}
	const int WARNING_OK = 3;
	XLOPER12 retVal;
	Excel12f(xlcAlert, &retVal, 2, TempStr12(szWarningMessage), TempInt12(WARNING_OK));
}

/**
* Display a message box with a warning/error icon with the provided message.  This will block
* execution until the user clicks OK.
* @param szWarningMessage a pointer to a null-terminated wide C-string containing
*                         the message to display
*/
void ExcelUtils::ErrorMessageBox(wchar_t *szWarningMessage) {
	if (g_pJvmEnv) {
		g_pJvmEnv->HideSplash();
	}
	const int ERROR_OK = 3;
	XLOPER12 retVal;
	Excel12f(xlcAlert, &retVal, 2, TempStr12(szWarningMessage), TempInt12(ERROR_OK));
}

/**
* Display a message box with a info icon with the provided message.  This will block
* execution until the user clicks OK.
* @param szWarningMessage a pointer to a null-terminated wide C-string containing
*                         the message to display
*/
void ExcelUtils::InfoMessageBox(wchar_t *szWarningMessage) {
	if (g_pJvmEnv) {
		g_pJvmEnv->HideSplash();
	}
	const int INFO_OK = 2;
	XLOPER12 retVal;
	Excel12f(xlcAlert, &retVal, 2, TempStr12(szWarningMessage), TempInt12(INFO_OK));
}

/**
 * Get Excel's Window handle
 * @param phWnd
 */
BOOL ExcelUtils::GetHWND (HWND *phWnd) {
	XLOPER12 xWnd;
	if (Excel12f (xlGetHwnd, &xWnd, 0) == xlretSuccess) {
		*phWnd = reinterpret_cast<HWND>(xWnd.val.w);
		Excel12f (xlFree, 0, 1, static_cast<LPXLOPER12>(&xWnd));
		return TRUE;
	}
	return FALSE;
}

BOOL GetHwnd (HWND * pHwnd) {
	XLOPER12 x;
	if (Excel12f (xlGetHwnd, &x, 0) == xlretSuccess) {
		*pHwnd = (HWND)x.val.w;
		return TRUE;
	}
	return FALSE;
}

BOOL ExcelUtils::IsAddinSettingEnabled (const wchar_t *wsSettingName, const BOOL bDefaultIfMissing) {
	_std_string_t settingName = _std_string_t (wsSettingName);
	CSettings *pSettings;
	if (FAILED(g_pAddinEnv->GetSettings(&pSettings))) {
		LOGWARN("Could not get valid settings from add-in environment, falling back to default");
		return bDefaultIfMissing;
	}
	const _bstr_t value = pSettings->GetString (ADDIN_SETTINGS, settingName);
	if (value.length () == 0) {
		return bDefaultIfMissing;
	}
	return value == _bstr_t("Enabled");
}

bstr_t ExcelUtils::GetAddinSetting (const wchar_t *wsSettingName, const wchar_t* wsDefaultIfMissing) {
	_std_string_t settingName = _std_string_t (wsSettingName);
	CSettings *pSettings;
	if (FAILED(g_pAddinEnv->GetSettings(&pSettings))) {
		LOGWARN("Could not get valid settings from add-in environment, falling back to default");
		return _bstr_t(wsDefaultIfMissing);
	}
	const _bstr_t value = pSettings->GetString (ADDIN_SETTINGS, settingName);
	if (value.length () == 0) {
		return _bstr_t(wsDefaultIfMissing);
	}
	return value;
}

const wchar_t *ExcelUtils::ADDIN_SETTINGS = L"AddinSettings";

void ExcelUtils::PrintExcel12Error (int err) {
	switch (err) {
	case xlretSuccess:
		LOGTRACE ("xlretSuccess(%d) returned by Excel12", err);
		break;
	case xlretAbort:
		LOGERROR ("xlretAbort(%d) returned by Excel12 (internal abort)", err);
		break;
	case xlretInvXlfn:
		LOGERROR ("xlretInvXlfn(%d) returned by Excel12 (invalid function number was supplied)", err);
		break;
	case xlretInvCount:
		LOGERROR ("xlretInvCount(%d) returned by Excel12 (invalid number of arguments was entered)", err);
		break;
	case xlretInvXloper:
		LOGERROR ("xlretInvXloper(%d) returned by Excel12 (invalid XLOPER/XLOPER12 was passed or an argument was of the wrong type)", err);
		break;
	case xlretStackOvfl:
		LOGERROR ("xlretStackOvfl(%d) returned by Excel12 (a stack overflow occurred)", err);
		break;
	case xlretFailed:
		LOGERROR ("xlretFailed(%d) returned by Excel12 (a command-equivalent function failed)", err);
		break;
	case xlretUncalced:
		LOGERROR ("xlretUncalced(%d) returned by Excel12 (attempt to dereference cell that hasn't been calculated yet)", err);
		break;
	case xlretNotThreadSafe:
		LOGERROR ("xlretNotThreadSafe(%d) returned by Excel12 (attempt made to call function that might not be thread safe during MT recalc)", err);
		break;
	case xlretInvAsynchronousContext:
		LOGERROR ("xlretInvAsynchronousContext(%d) returned by Excel12 (the asynchronous function handle is invalid)", err);
		break;
	case xlretNotClusterSafe:
		LOGERROR ("xlretNotClusterSafe(%d) returned by Excel12 (The call is not supported on clusters)", err);
		break;
	default:
		LOGERROR ("Unknown error code (%d) returned by Excel12", err);
		break;
	}
}

void ExcelUtils::PasteTool(LPCWSTR lpBitmapName, int index) {
	HBITMAP hBitmap = LoadBitmapW((HINSTANCE)g_hInst, lpBitmapName);
	if (hBitmap == nullptr) {
		_com_error err(HRESULT_FROM_WIN32(GetLastError()));
		LOGERROR("Problem loading settings icon bitmap: %s", err.ErrorMessage());
		return;
	}
	if (!OpenClipboard(nullptr)) { LOGERROR("Could not open clipboard"); return; }
	if (!EmptyClipboard()) { LOGERROR("Could not empty clipboard"); CloseClipboard();  return; }
	if (!SetClipboardData(CF_BITMAP, hBitmap)) { LOGERROR("Could not put settings icon bitmap onto clipboard"); }
	if (!CloseClipboard()) { LOGERROR("Could not close clipboard"); return; }
	Excel12f(xlcPasteTool, 0, 2, TempStr12(L"XL4J"), TempInt12(index));
	if (!OpenClipboard(nullptr)) { LOGERROR("Could not open clipboard"); return; }
	if (!EmptyClipboard()) { LOGERROR("Could not empty clipboard"); }
	if (!CloseClipboard()) { LOGERROR("Could not close clipboard"); return; }
}