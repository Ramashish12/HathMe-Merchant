package code.utils;

import android.app.Activity;

public final class AppSettings extends OSettings {
    public static final String PREFS_MAIN_FILE          = "PREFS_HATHME_MERCHANT_FILE";
    public static final String userId                   = "userId";
    public static final String loginId                   = "loginId";
    public static final String fcmToken                  = "fcmToken";
    public static final String name                  = "name";
    public static final String mobile                  = "mobile";
    public static final String email                  = "email";
    public static final String isProfileCompleted                  = "isProfileCompleted";
    public static final String isBusinessSave                  = "isBusinessSave";
    public static final String isCategorySelected                  = "isCategorySelected";
    public static final String isMobileVerified                  = "isMobileVerified";
    public static final String token = "token";
    public static final String imagePath = "imagePath";
    public static final String profileImage = "profileImage";
    public static final String updatedProfileImage = "updatedProfileImage";
    public static final String currentDate = "currentDate";
    public static final String language = "language";
    public static final String countrySymbol = "countrySymbol";
    public static final String KEY_selected_image = "selected_image";
    public static final String KEY_selected_type = "selected_type";
    public static final String KEY_selected_filename = "selected_filename";
    public static final String KEY_selected_url = "selected_url";
    public static final String KEY_selected_pdfurl = "selected_pdfurl";
    public static final String isFrom = "isFrom";
    public static final String isUpdateImage = "isUpdateImage";
    public static final String documentStatus = "documentStatus";
    public static final String documentNumber = "documentNumber";
    public static final String documentFrontImage = "documentFrontImage";
    public static final String documentBackImage = "documentBackImage";
    public static final String documentId = "documentId";
    public static final String isWithdrawPinCreated = "isWithdrawPinCreated";




    public AppSettings(Activity mActivity) {
        super(mActivity);
    }
}
