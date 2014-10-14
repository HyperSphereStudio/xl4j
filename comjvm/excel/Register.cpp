#include "stdafx.h"
#include "Register.h"
#include "utils/Debug.h"
#include "helper/JniSequenceHelper.h"
#include "helper/ClasspathUtils.h"

using std::cerr;
using std::cout;
using std::endl;

COMJVM_EXCEL_API Register::Register (IJvm *pJvm) {
	m_pJvm = pJvm;
	m_pJvm->AddRef ();
}

void COMJVM_EXCEL_API Register::scanAndRegister (XLOPER12 xDLL) {
	try { 
		JniSequenceHelper *helper = new JniSequenceHelper (m_pJvm);
		long excel = helper->CallStaticMethod (JTYPE_OBJECT, TEXT ("com/mcleodmoores/excel4j/ExcelFactory"), TEXT ("getInstance"), TEXT ("()Lcom/mcleodmoores/excel4j/Excel;"), 0);
		long nativeExcelClsId = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/Excel"));
		long excelCallback = helper->CallMethod (JTYPE_OBJECT, excel, helper->GetMethodID (nativeExcelClsId, TEXT ("getExcelCallback"), TEXT ("()Lcom/mcleodmoores/excel4j/callback/ExcelCallback;")), 0);
		long functionRegistry = helper->CallMethod (JTYPE_OBJECT, excel, helper->GetMethodID (nativeExcelClsId, TEXT ("getFunctionRegistry"), TEXT ("()Lcom/mcleodmoores/excel4j/FunctionRegistry;")), 0);
		long functionRegistryClsId = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/FunctionRegistry"));
		helper->CallMethod (JTYPE_VOID, functionRegistry, helper->GetMethodID (functionRegistryClsId, TEXT ("registerFunctions"), TEXT ("(Lcom/mcleodmoores/excel4j/callback/ExcelCallback;)V")), 1, excelCallback);
		long lowLevelExcelCallback = helper->CallMethod (JTYPE_OBJECT, excel, helper->GetMethodID (nativeExcelClsId, TEXT ("getLowLevelExcelCallback"), TEXT ("()Lcom/mcleodmoores/excel4j/lowlevel/LowLevelExcelCallback;")), 0);
		long xllAccumulatingFunctionRegistryClsId = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry"));
		long registerArr = helper->CallMethod (JTYPE_OBJECT, lowLevelExcelCallback, helper->GetMethodID (xllAccumulatingFunctionRegistryClsId, TEXT ("getEntries"), TEXT ("()[Lcom/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry;")), 0);
		long gRegisterArr = helper->NewGlobalRef (registerArr);
		long arrSize = helper->GetArrayLength (registerArr);
		helper->Result (gRegisterArr);
		helper->Result (arrSize);
		VARIANT results[2];
		helper->Execute (0, NULL, 2, results);
		int size = results[1].intVal;
		
		TRACE ("Number of entries was %d", size);
		for (int i = 0; i < size; i++) {
			long gArrayRef = helper->Argument ();
			long entryObj = helper->GetObjectArrayElement (gArrayRef, (long) i);
			TCHAR *lowLevelEntryName = TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry");
			long entryCls = helper->FindClass (lowLevelEntryName);
			// queue up the sequence to extract global refs for each String field + pjchar references
			extractField (helper, JTYPE_INT, entryCls, entryObj, TEXT ("_exportNumber"), TEXT ("I"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_functionExportName"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_functionSignature"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_functionWorksheetName"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_argumentNames"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_INT, entryCls, entryObj, TEXT ("_functionType"), TEXT ("I"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_functionCategory"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_acceleratorKey"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_helpTopic"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_description"), TEXT ("Ljava/lang/String;"));
			// queue up request for global ref of argsHelp + argsHelp.length
			long argsHelpArr = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (lowLevelEntryName, TEXT ("_argsHelp"), TEXT ("[Ljava/lang/String;")));
			long gArgsHelpArr = helper->NewGlobalRef (argsHelpArr);
			helper->Result (gArgsHelpArr);
			long argsHelpArrLength = helper->GetArrayLength (argsHelpArr);
			helper->Result (argsHelpArrLength);
			VARIANT entryResults[12];
			VARIANT *args = results; // alias to the outer results, we only want the first value passed in, so we pass length of 1.

			helper->Execute (1, args, 12, entryResults);
			// pull out all the fields from the results
			int exportNumber = entryResults[0].intVal;
			bstr_t functionExportName = entryResults[1].bstrVal;
			bstr_t functionSignature = entryResults[2].bstrVal;
			bstr_t worksheetName = entryResults[3].bstrVal;
			bstr_t argumentNames = entryResults[4].bstrVal;
			int functionType = entryResults[5].intVal; // note there's only one ref here, not two.
			bstr_t functionCategory = entryResults[6].bstrVal;
			bstr_t acceleratorKey = entryResults[7].bstrVal;
			bstr_t helpTopic = entryResults[8].bstrVal;
			bstr_t description = entryResults[9].bstrVal;
			// get the argsHelp array size
			int argsHelpSz = entryResults[11].intVal;
			VARIANT *argsHelpArrResults = new VARIANT[argsHelpSz]; // one for jstring and one for pjchar so we can release them afterwards.
			long argsHelpArrRef = helper->Argument ();

			for (int argsHelpIndex = 0; argsHelpIndex < argsHelpSz; argsHelpIndex++) {
				long argsHelpStrObj = helper->GetObjectArrayElement (argsHelpArrRef, argsHelpIndex);
				long isCopy;
				long argsHelpPjcharRef = helper->GetStringChars (argsHelpStrObj, &isCopy);
				helper->Result (argsHelpPjcharRef);
				helper->ReleaseStringChars (argsHelpStrObj, argsHelpPjcharRef);
			}
			helper->DeleteGlobalRef (argsHelpArrRef);
			helper->Execute (1, &entryResults[10], argsHelpSz, argsHelpArrResults);
			// copy out.
			bstr_t *argsHelp = new bstr_t[argsHelpSz];
			for (int m = 0; m < argsHelpSz; m++) {
				argsHelp[m] = argsHelpArrResults[m].bstrVal;
			}
			registerFunction (xDLL, exportNumber, functionExportName, functionSignature, worksheetName, argumentNames, functionType, functionCategory, acceleratorKey, helpTopic, description, argsHelpSz, argsHelp);
			delete [] argsHelp;
			delete [] argsHelpArrResults;
		}
		delete helper;
	}
	catch (_com_error& e) {
		TRACE ("Caught exception: %s, description: %s, code: %x", e.ErrorMessage (), e.Description(), e.Error());
	}
}

void COMJVM_EXCEL_API Register::registerFunction (XLOPER12 xDll, int functionExportNumber, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
	bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp) {
	TRACE ("functionExportNumber=%d,functionExportName=%s\nfunctionSignature=%s\nworksheetName=%s\nargumentNames=%s\nfunctionType=%d\nfunctionCategory=%s\nacceleratorKey=%s\nhelpTopic=%s\ndescription=%s\nargsHelpSz=%d",
		functionExportNumber, (wchar_t *)functionExportName, (wchar_t *)functionSignature, (wchar_t *)worksheetName, (wchar_t *)argumentNames, (wchar_t *)functionType, (wchar_t *)functionCategory, (wchar_t *)acceleratorKey, (wchar_t *)helpTopic, (wchar_t *)description, (wchar_t *)argsHelpSz);
	LPXLOPER12 *args = new LPXLOPER12[10 + argsHelpSz];
	args[0] = (LPXLOPER12)&xDll;
	args[1] = (LPXLOPER12)TempStr12 (functionExportName);
	args[2] = (LPXLOPER12)TempStr12 (functionSignature);
	args[3] = (LPXLOPER12)TempStr12 (worksheetName);
	args[4] = (LPXLOPER12)TempStr12 (argumentNames);
	args[5] = (LPXLOPER12)TempInt12 (functionType);
	args[6] = (LPXLOPER12)TempStr12 (functionCategory);
	args[7] = (LPXLOPER12)TempStr12 (acceleratorKey);
	args[8] = (LPXLOPER12)TempStr12 (helpTopic);
	args[9] = (LPXLOPER12)TempStr12 (description);
	for (int i = 0; i < argsHelpSz; i++) {
		args[10 + i] = (LPXLOPER12)TempStr12 (argsHelp[i]);
	}
	m_numArgsForExport[functionExportNumber] = argsHelpSz; // num args
	Excel12v (xlfRegister, 0, 10 + argsHelpSz, args);
	delete[] args;
}

void COMJVM_EXCEL_API Register::extractField (JniSequenceHelper *helper, long fieldType, long entryCls, long entryObj, TCHAR *fieldName, TCHAR *signature) {
	long field = helper->GetField (fieldType, entryObj, helper->GetFieldID (entryCls, fieldName, signature));
	if (fieldType == JTYPE_OBJECT) {
		long isCopy;
		long fieldStr = helper->GetStringChars (field, &isCopy);
		helper->Result (fieldStr);
		helper->ReleaseStringChars (field, fieldStr);
	} else {
		helper->Result (field);
	}
}

COMJVM_EXCEL_API Register::~Register () {
	m_pJvm->Release ();
}