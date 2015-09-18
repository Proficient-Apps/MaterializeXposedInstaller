package in.proficientapps.MaterializeXposedInstaller;

import android.app.Activity;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/*
 * Created by SArnab©® on 06-09-2015.
 *
 * Developer Note : I made this Module in 10 Minutes
 * so that I can enjoy Material UI inside the Xposed
 * Installer App. For now it's very basic and provides
 * 3 different themes to choose from. Feel free to fork
 * and upgrade it if you have anything new in mind.
 * But, please do not release a new separate version.
 * Instead, update me regarding your changes and I will try my best
 * to add those in my source and release an update.
 * You may compile a different version for your personal use.
 */
public class Module implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

    public static String MODULE_PATH = null;
    public static XSharedPreferences pref;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        pref = new XSharedPreferences("in.proficientapps.MaterializeXposedInstaller");
    }

    /* Fixing Cards BG issue in Dark Theme(s). */
    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        pref.reload();
        /* Checking if it's really Xposed Installer App or not. */
        if (!resparam.packageName.equals("de.robv.android.xposed.installer"))
            return;

        XModuleResources modRes = XModuleResources.createInstance(Module.MODULE_PATH, resparam.res);

        /* Checking if the module has been activated within the app. */
        if (pref.getBoolean("pref_key_activate_module", false)) {
            /* Getting selected colour from ColourPickerPreference */
            int mPrimaryColour = pref.getInt("pref_key_colour_primary", R.color.colorAccent0);
            final int mAccentColour = pref.getInt("pref_key_colour_accent", R.color.colorAccent1);
            int mBgColour = pref.getInt("pref_key_colour_bg", R.color.colorAccent2);
            /* Creating PrimaryColourDark from PrimaryColour */
            int mPrimaryColourDark = Color.rgb(Color.red(mPrimaryColour) * 192 / 256, Color.green(mPrimaryColour) * 192 / 256, Color.blue(mPrimaryColour) * 192 / 256);
            /* Getting selected theme index. */
            int transition = Integer.parseInt(pref.getString("pref_key_theme_list", "0"));
            switch (transition) {
                case 0:
                    break;

                case 1:
                   /*
                    * Added in version 1.1
                    * Checking if Colour Picker CheckBox is checked or not.
                    * If checked than app's primary and primary dark colours are applied from ColourPickerPreference.
                    * Same occurs in all other cases.
                    */
                    if(pref.getBoolean("pref_key_colour_primary_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "primary_material_dark", mPrimaryColour);
                        XResources.setSystemWideReplacement("android", "color", "primary_dark_material_dark", mPrimaryColourDark);
                    }
                    if(pref.getBoolean("pref_key_colour_accent_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "material_deep_teal_500", mAccentColour);
                        resparam.res.hookLayout("android", "layout", "preference_category_material", new XC_LayoutInflated() {
                            @Override
                            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                                TextView preferenceCategory = (TextView) liparam.view.getRootView();
                                preferenceCategory.setTextColor(mAccentColour);
                            }
                        });
                    }
                    if(pref.getBoolean("pref_key_colour_bg_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "background_material_light", mBgColour);
                    }
                    break;

                case 2:
                    /* Added in version v1.1 */
                    if(pref.getBoolean("pref_key_colour_primary_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "primary_material_light", mPrimaryColour);
                        XResources.setSystemWideReplacement("android", "color", "primary_dark_material_light", mPrimaryColourDark);
                    }
                    if(pref.getBoolean("pref_key_colour_accent_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "material_deep_teal_500", mAccentColour);
                        resparam.res.hookLayout("android", "layout", "preference_category_material", new XC_LayoutInflated() {
                            @Override
                            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                                TextView preferenceCategory = (TextView) liparam.view.getRootView();
                                preferenceCategory.setTextColor(mAccentColour);
                            }
                        });
                    }
                    if(pref.getBoolean("pref_key_colour_bg_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "background_material_light", mBgColour);
                    }
                    break;

                case 3:
                    /* Only the last theme requires the fix as the text is white on white bg in Modules Page */
                    resparam.res.setReplacement("de.robv.android.xposed.installer", "drawable", "background_card_light", modRes.fwd(R.drawable.background_card_dark));
                    resparam.res.setReplacement("de.robv.android.xposed.installer", "drawable", "background_card_pressed_light", modRes.fwd(R.drawable.background_card_pressed_dark));
                    /* Added in version v1.1 */
                    if(pref.getBoolean("pref_key_colour_primary_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "primary_material_dark", mPrimaryColour);
                        XResources.setSystemWideReplacement("android", "color", "primary_dark_material_dark", mPrimaryColourDark);
                    }
                    if(pref.getBoolean("pref_key_colour_accent_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "material_deep_teal_200", mAccentColour);
                        resparam.res.hookLayout("android", "layout", "preference_category_material", new XC_LayoutInflated() {
                            @Override
                            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                                TextView preferenceCategory = (TextView) liparam.view.getRootView();
                                preferenceCategory.setTextColor(mAccentColour);
                            }
                        });
                    }
                    if(pref.getBoolean("pref_key_colour_bg_checkbox", false)) {
                        XResources.setSystemWideReplacement("android", "color", "background_material_dark", mBgColour);
                    }
                    break;
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        pref.reload();
        /* Checking if it's really Xposed Installer App or not. */
        if (!lpparam.packageName.equals("de.robv.android.xposed.installer"))
            return;

        /* Setting the base activity class to hook it for making changes. */
        final Class<?> XposedInstallerBase = findClass("de.robv.android.xposed.installer.XposedBaseActivity", lpparam.classLoader);

        try {
            findAndHookMethod(XposedInstallerBase, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    /* Getting the base activity */
                    Activity activity = (Activity) param.thisObject;
                    /* Checking if the module has been activated within the app. */
                    if (pref.getBoolean("pref_key_activate_module", false)) {
                        /* Getting selected colour from ColourPickerPreference */
                        int mPrimaryColour = pref.getInt("pref_key_colour_primary", R.color.colorAccent2);
                        /* Creating PrimaryColourDark from PrimaryColour */
                        int mPrimaryColourDark = Color.rgb(Color.red(mPrimaryColour) * 192 / 256, Color.green(mPrimaryColour) * 192 / 256, Color.blue(mPrimaryColour) * 192 / 256);
                        /* Getting selected theme index. */
                        int transition = Integer.parseInt(pref.getString("pref_key_theme_list", "0"));
                        switch (transition) {
                            case 0:
                                break;

                            case 1:
                                activity.setTheme(android.R.style.Theme_Material_Light_DarkActionBar);
                                /* Added in version v1.1 */
                                /*
                                 * Checking if Colour Picker CheckBox is checked or not.
                                 * If checked than change statusbar colour to PrimaryDark.
                                 * Same occurs in all other cases.
                                 */
                                if(pref.getBoolean("pref_key_colour_primary_checkbox", false)) {
                                    Window window = activity.getWindow();
                                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                    window.setStatusBarColor(mPrimaryColourDark);
                                }
                                break;

                            case 2:
                                activity.setTheme(android.R.style.Theme_Material_Light);
                                /* Added in version v1.1 */
                                if(pref.getBoolean("pref_key_colour_primary_checkbox", false)) {
                                    Window window = activity.getWindow();
                                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                    window.setStatusBarColor(mPrimaryColourDark);
                                }
                                break;

                            case 3:
                                activity.setTheme(android.R.style.Theme_Material);
                                /* Added in version v1.1 */
                                if(pref.getBoolean("pref_key_colour_primary_checkbox", false)) {
                                    Window window = activity.getWindow();
                                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                    window.setStatusBarColor(mPrimaryColourDark);
                                }
                                break;
                        }
                    }
                }
            });
        } catch (Exception e) {
            /* Doing everything inside a try-catch block in order to get any error that occurs. */
            XposedBridge.log("================================\nError Message : " + e.getMessage());
            XposedBridge.log("Error Cause" + e.getCause().toString() + "\n================================");
        }
    }
}
