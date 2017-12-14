package com.android.camera.aosp_porting;

import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.camera.Device;

public class Build extends android.os.Build {
    public static final boolean HAS_CUST_PARTITION = SystemProperties.getBoolean("ro.miui.has_cust_partition", false);
    public static final boolean IS_ALPHA_BUILD = SystemProperties.get("ro.product.mod_device", "").endsWith("_alpha");
    public static final boolean IS_CDMA;
    public static final boolean IS_CM_COOPERATION = ("cm".equals(SystemProperties.get("ro.carrier.name")) ? "cn_cmcooperation".equals(SystemProperties.get("ro.miui.cust_variant")) : false);
    public static final boolean IS_CM_CUSTOMIZATION = ("cm".equals(SystemProperties.get("ro.carrier.name")) ? "cn_chinamobile".equals(SystemProperties.get("ro.miui.cust_variant")) : false);
    public static final boolean IS_CM_CUSTOMIZATION_TEST = "cm".equals(SystemProperties.get("ro.cust.test"));
    public static final boolean IS_CTA_BUILD = "1".equals(SystemProperties.get("ro.miui.cta"));
    public static final boolean IS_CTS_BUILD = (!SystemProperties.getBoolean("persist.sys.miui_optimization", !"1".equals(SystemProperties.get("ro.miui.cts"))));
    public static final boolean IS_CT_CUSTOMIZATION = "ct".equals(SystemProperties.get("ro.carrier.name"));
    public static final boolean IS_CT_CUSTOMIZATION_TEST = "ct".equals(SystemProperties.get("ro.cust.test"));
    public static final boolean IS_CU_CUSTOMIZATION = "cu".equals(SystemProperties.get("ro.carrier.name"));
    public static final boolean IS_CU_CUSTOMIZATION_TEST = "cu".equals(SystemProperties.get("ro.cust.test"));
    public static final boolean IS_DEBUGGABLE;
    public static final boolean IS_DEVELOPMENT_VERSION = (!TextUtils.isEmpty(VERSION.INCREMENTAL) ? VERSION.INCREMENTAL.matches("\\d+.\\d+.\\d+(-internal)?") : false);
    public static final boolean IS_FUNCTION_LIMITED = "1".equals(SystemProperties.get("persist.sys.func_limit_switch"));
    public static final boolean IS_GLOBAL_BUILD = SystemProperties.get("ro.product.mod_device", "").endsWith("_global");
    public static final boolean IS_HONGMI;
    public static final boolean IS_HONGMI2_TDSCDMA = "HM2013022".equals(DEVICE);
    public static final boolean IS_HONGMI_THREE = (!"lcsh92_wet_jb9".equals(DEVICE) ? "lcsh92_wet_tdd".equals(DEVICE) : true);
    public static final boolean IS_HONGMI_THREEX = "gucci".equals(DEVICE);
    public static final boolean IS_HONGMI_THREEX_CM = (IS_HONGMI_THREEX ? "cm".equals(SystemProperties.get("persist.sys.modem")) : false);
    public static final boolean IS_HONGMI_THREEX_CT = (IS_HONGMI_THREEX ? "ct".equals(SystemProperties.get("persist.sys.modem")) : false);
    public static final boolean IS_HONGMI_THREEX_CU = (IS_HONGMI_THREEX ? "cu".equals(SystemProperties.get("persist.sys.modem")) : false);
    public static final boolean IS_HONGMI_THREE_LTE = "dior".equals(DEVICE);
    public static final boolean IS_HONGMI_THREE_LTE_CM = (IS_HONGMI_THREE_LTE ? "LTETD".equals(SystemProperties.get("ro.boot.modem")) : false);
    public static final boolean IS_HONGMI_THREE_LTE_CU = (IS_HONGMI_THREE_LTE ? "LTEW".equals(SystemProperties.get("ro.boot.modem")) : false);
    public static final boolean IS_HONGMI_TWO;
    public static final boolean IS_HONGMI_TWOS_LTE_MTK = "HM2014501".equals(DEVICE);
    public static final boolean IS_HONGMI_TWOX;
    public static final boolean IS_HONGMI_TWOX_BR = "HM2014819".equals(DEVICE);
    public static final boolean IS_HONGMI_TWOX_CM = (!"HM2014813".equals(DEVICE) ? "HM2014112".equals(DEVICE) : true);
    public static final boolean IS_HONGMI_TWOX_CT = (!"HM2014812".equals(DEVICE) ? "HM2014821".equals(DEVICE) : true);
    public static final boolean IS_HONGMI_TWOX_CU = "HM2014811".equals(DEVICE);
    public static final boolean IS_HONGMI_TWOX_IN = "HM2014818".equals(DEVICE);
    public static final boolean IS_HONGMI_TWOX_LC = "lte26007".equals(DEVICE);
    public static final boolean IS_HONGMI_TWOX_SA = "HM2014817".equals(DEVICE);
    public static final boolean IS_HONGMI_TWO_A = "armani".equals(DEVICE);
    public static final boolean IS_HONGMI_TWO_S = (!"HM2014011".equals(DEVICE) ? "HM2014012".equals(DEVICE) : true);
    public static final boolean IS_INTERNATIONAL_BUILD = SystemProperties.get("ro.product.mod_device", "").contains("_global");
    public static final boolean IS_MI1S = (!"MI 1S".equals(MODEL) ? "MI 1SC".equals(MODEL) : true);
    public static final boolean IS_MI2A = (!"MI 2A".equals(MODEL) ? "MI 2A TD".equals(MODEL) : true);
    public static final boolean IS_MIFIVE = "virgo".equals(DEVICE);
    public static final boolean IS_MIFOUR = ("cancro".equals(DEVICE) ? MODEL.startsWith("MI 4") : false);
    public static final boolean IS_MIFOUR_CDMA = (IS_MIFOUR ? "CDMA".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_MIFOUR_LTE_CM = (IS_MIFOUR ? "LTE-CMCC".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_MIFOUR_LTE_CT = (IS_MIFOUR ? "LTE-CT".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_MIFOUR_LTE_CU = (IS_MIFOUR ? "LTE-CU".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_MIFOUR_LTE_INDIA = (IS_MIFOUR ? "LTE-India".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_MIFOUR_LTE_SEASA = (IS_MIFOUR ? "LTE-SEAsa".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_MIONE = (!"mione".equals(DEVICE) ? "mione_plus".equals(DEVICE) : true);
    public static final boolean IS_MIONE_CDMA = (IS_MIONE ? hasMsm8660Property() : false);
    public static final boolean IS_MIPAD = "mocha".equals(DEVICE);
    public static final boolean IS_MITHREE;
    public static final boolean IS_MITHREE_CDMA = (IS_MITHREE ? "MI 3C".equals(MODEL) : false);
    public static final boolean IS_MITHREE_TDSCDMA = (IS_MITHREE ? "TD".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_MITWO;
    public static final boolean IS_MITWO_CDMA = (IS_MITWO ? "CDMA".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_MITWO_TDSCDMA = (IS_MITWO ? "TD".equals(SystemProperties.get("persist.radio.modem")) : false);
    public static final boolean IS_N7 = "flo".equals(DEVICE);
    public static final boolean IS_OFFICIAL_VERSION = (!IS_DEVELOPMENT_VERSION ? IS_STABLE_VERSION : true);
    public static final boolean IS_PRO_DEVICE = SystemProperties.get("ro.miui.cust_device", "").endsWith("_pro");
    public static final boolean IS_STABLE_VERSION;
    public static final boolean IS_TABLET = isTablet();
    public static final boolean IS_TDS_CDMA;
    public static final boolean IS_XIAOMI;
    public static final String USERDATA_IMAGE_VERSION_CODE = getUserdataImageVersionCode();

    static {
        boolean z = true;
        boolean equals = ("aries".equals(DEVICE) || "taurus".equals(DEVICE)) ? true : "taurus_td".equals(DEVICE);
        IS_MITWO = equals;
        equals = !"pisces".equals(DEVICE) ? "cancro".equals(DEVICE) ? MODEL.startsWith("MI 3") : false : true;
        IS_MITHREE = equals;
        equals = (IS_MIONE || IS_MITWO || IS_MITHREE || IS_MIFOUR) ? true : IS_MIFIVE;
        IS_XIAOMI = equals;
        equals = ("HM2013022".equals(DEVICE) || "HM2013023".equals(DEVICE) || IS_HONGMI_TWO_A) ? true : IS_HONGMI_TWO_S;
        IS_HONGMI_TWO = equals;
        equals = (IS_HONGMI_TWOX_CU || IS_HONGMI_TWOX_CT || IS_HONGMI_TWOX_CM || IS_HONGMI_TWOX_IN || IS_HONGMI_TWOX_SA) ? true : IS_HONGMI_TWOX_BR;
        IS_HONGMI_TWOX = equals;
        equals = (IS_HONGMI_TWO || IS_HONGMI_THREE || IS_HONGMI_TWOX || IS_HONGMI_THREE_LTE || IS_HONGMI_TWOX_LC || IS_HONGMI_TWOS_LTE_MTK) ? true : IS_HONGMI_THREEX;
        IS_HONGMI = equals;
        equals = (IS_MIONE_CDMA || IS_MITWO_CDMA || IS_MITHREE_CDMA || IS_MIFOUR_CDMA) ? true : IS_MIFOUR_LTE_CT;
        IS_CDMA = equals;
        equals = (IS_MITHREE_TDSCDMA || IS_HONGMI2_TDSCDMA) ? true : IS_MITWO_TDSCDMA;
        IS_TDS_CDMA = equals;
        equals = "user".equals(TYPE) ? !IS_DEVELOPMENT_VERSION : false;
        IS_STABLE_VERSION = equals;
        if (SystemProperties.getInt("ro.debuggable", 0) != 1) {
            z = false;
        }
        IS_DEBUGGABLE = z;
    }

    protected Build() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate utility class");
    }

    private static String getUserdataImageVersionCode() {
        if ("".equals(SystemProperties.get("ro.miui.userdata_version", ""))) {
            return "Unavailable";
        }
        String str = Device.isGlobalBuild() ? "global" : "cn";
        String str2 = SystemProperties.get("ro.carrier.name", "");
        if (!"".equals(str2)) {
            str2 = "_" + str2;
        }
        return String.format("%s(%s%s)", new Object[]{r2, str, str2});
    }

    private static boolean hasMsm8660Property() {
        String str = SystemProperties.get("ro.soc.name");
        return !"msm8660".equals(str) ? "unkown".equals(str) : true;
    }

    private static boolean isTablet() {
        return SystemProperties.get("ro.build.characteristics").contains("tablet");
    }
}
