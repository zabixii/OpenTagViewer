from enum import Enum
import json
import traceback
from datetime import datetime, timezone
from io import BytesIO
import base64
import NSKeyedUnArchiver

from findmy import FindMyAccessory
from findmy.reports import (
    RemoteAnisetteProvider,
    AppleAccount,
    LoginState,
    SmsSecondFactorMethod,
    TrustedDeviceSecondFactorMethod,
)
from findmy.reports.twofactor import SyncSecondFactorMethod


class TwoFactorMethods(Enum):
    UNKNOWN = 0
    TRUSTED_DEVICE = 1
    PHONE = 2


def _toUnixEpochMs(dt: datetime) -> int:
    if dt is None:
        return None
    return int(dt.timestamp() * 1000)


def foo(arg: str):
    """For testing..."""
    print("Bar!")
    print(f"The arg was: '{arg}'")

    for i in range(10):
        print(f"Hello {i}!")

    print("Done!")

    return {
        "Some": "Dictionary",
        "key": [1, 2, 3],
        "other": False,
        "and": True,
        "nested": {
            "a": "b",
            "c": "d"
        },
        "set": {"a", "b", "c"},
        "floats": 1.123456,
        "null maybe": None
    }


def decodeBeaconNamingRecordCloudKitMetadata(cleanedBase64: str) -> dict:
    """
    Extract some extra information from within the plist file `cloudKitMetadata` node.
    """
    try:
        data = base64.b64decode(cleanedBase64)
        d_dict = NSKeyedUnArchiver.unserializeNSKeyedArchiver(data)

        record_ctime: datetime = d_dict.get("RecordCtime", None)
        record_mtime: datetime = d_dict.get("RecordMtime", None)
        modified_by_device: str = d_dict.get("ModifiedByDevice", None)

        res = {
            "creationTime": _toUnixEpochMs(record_ctime),
            "modifiedTime": _toUnixEpochMs(record_mtime),
            "modifiedByDevice": modified_by_device
        }

        print(f"Computed result: {res}")
        return res

    except Exception:
        print(f"Failed to parse due to {traceback.format_exc()}")
        return None


def _convertToJavaDictWrapper(method: SyncSecondFactorMethod):
    return_obj = {
        "obj": method
    }

    print(f"The input is {method} of class {type(method)}")

    if isinstance(method, TrustedDeviceSecondFactorMethod):
        print("Option: Trusted Device 2FA method")
        return_obj["type"] = TwoFactorMethods.TRUSTED_DEVICE.value
    elif isinstance(method, SmsSecondFactorMethod):
        print(f"Option: SMS ({method.phone_number})")
        return_obj["type"] = TwoFactorMethods.PHONE.value
        return_obj["phoneNumber"] = method.phone_number
        return_obj["phoneNumberId"] = method.phone_number_id
    else:
        print(f"Unmapped 2FA method! (type: {type(method)})")
        return_obj["type"] = TwoFactorMethods.UNKNOWN.value

    return return_obj


def loginSync(email: str, password: str, anisetteServerUrl: str) -> dict:
    try:
        anisette = RemoteAnisetteProvider(anisetteServerUrl)
        acc = AppleAccount(anisette)

        state = acc.login(email, password)

        if state == LoginState.REQUIRE_2FA:
            methods = acc.get_2fa_methods()
            named_methods_list = []
            for method in methods:
                named_methods_list.append(_convertToJavaDictWrapper(method))

            return {
                "account": acc,
                "loginState": state.value,
                "loginMethods": named_methods_list
            }

        return {
            "account": acc,
            "loginState": state.value,
            "loginMethods": None
        }

    except Exception as e:
        print(f"Failed to log in due to error: {traceback.format_exc()}")
        return {
            "error": str(e)
        }


def exportToString(account: AppleAccount) -> str:
    return json.dumps(account.to_json())


def getAccount(serializedAccountData: str) -> AppleAccount:
    try:
        data = json.loads(serializedAccountData)
        acc = AppleAccount.from_json(data)
        print(f"Login State: {acc.login_state}")
        return acc
    except Exception:
        err = traceback.format_exc()
        print(f"Failed to restore account from string: {err}")
        return None


def convertPlistToJson(plistXmlString: str) -> str:
    try:
        fp = BytesIO(plistXmlString.encode("utf-8"))
        accessory = FindMyAccessory.from_plist(fp)
        return json.dumps(accessory.to_json())
    except Exception:
        print(f"convertPlistToJson failed: {traceback.format_exc()}")
        return None


def _filterReportsByTimeRange(reports, startMs, endMs):
    out = []
    for report in reports:
        ts_ms = _toUnixEpochMs(report.timestamp)
        if ts_ms is None:
            continue
        if startMs is not None and ts_ms < startMs:
            continue
        if endMs is not None and ts_ms > endMs:
            continue
        out.append(report)
    return out


def _serializeReports(reports):
    items = []
    for report in sorted(reports):
        items.append({
            "publishedAt": _toUnixEpochMs(report.timestamp),
            "description": getattr(report, "description", "") or "",
            "timestamp": _toUnixEpochMs(report.timestamp),
            "confidence": report.confidence,
            "latitude": report.latitude,
            "longitude": report.longitude,
            "horizontalAccuracy": report.horizontal_accuracy,
            "status": report.status
        })
    return items


def getLastReports(
        account: AppleAccount,
        idToAccessoryData,
        hoursBack: int) -> dict:
    try:
        res = {}

        num_items = idToAccessoryData.size()
        print(f"getLastReports: num_items={num_items}, hoursBack={hoursBack}")

        now_ms = int(datetime.now(tz=timezone.utc).timestamp() * 1000)
        start_ms = now_ms - (hoursBack * 60 * 60 * 1000)

        for i in range(0, num_items):
            req = idToAccessoryData.get(i)
            beaconId = req.getBeaconId()
            accessoryJson = req.getAccessoryJson()

            print(f"Fetching report for {beaconId} for the last {hoursBack} hours...")

            airtag = FindMyAccessory.from_json(json.loads(accessoryJson))
            reports = account.fetch_location_history(airtag)
            if reports is None:
                reports = []
            print(f"Got {len(reports)} raw reports for {beaconId}")

            filtered = _filterReportsByTimeRange(reports, start_ms, now_ms)
            print(f"  -> {len(filtered)} reports after filtering to last {hoursBack}h")

            res[beaconId] = {
                "reports": _serializeReports(filtered),
                "updatedAccessoryJson": json.dumps(airtag.to_json()),
            }

        return res

    except Exception:
        err = traceback.format_exc()
        print(f"Failed to fetch all reports due to error: {err}")
        return None


def getReports(
        account: AppleAccount,
        idToAccessoryData,
        unixStartMs: int,
        unixEndMs: int) -> dict:
    try:
        res = {}

        num_items = idToAccessoryData.size()
        print(f"getReports: num_items={num_items}, range=[{unixStartMs}, {unixEndMs}]")

        for i in range(0, num_items):
            req = idToAccessoryData.get(i)
            beaconId = req.getBeaconId()
            accessoryJson = req.getAccessoryJson()

            print(f"Fetching report for {beaconId} in time range {unixStartMs}-{unixEndMs}...")

            airtag = FindMyAccessory.from_json(json.loads(accessoryJson))
            reports = account.fetch_location_history(airtag)
            if reports is None:
                reports = []
            print(f"Got {len(reports)} raw reports for {beaconId}")

            filtered = _filterReportsByTimeRange(reports, unixStartMs, unixEndMs)
            print(f"  -> {len(filtered)} reports after filtering to requested range")

            res[beaconId] = {
                "reports": _serializeReports(filtered),
                "updatedAccessoryJson": json.dumps(airtag.to_json()),
            }

        return res

    except Exception:
        err = traceback.format_exc()
        print(f"Failed to fetch all reports due to error: {err}")
        return None
