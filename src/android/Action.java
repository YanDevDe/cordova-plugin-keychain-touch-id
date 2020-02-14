package com.cordova.plugin.android.biometricauth;

import android.support.annotation.Nullable;

public enum Action {
    SAVE("save") {
        @Override
        public ActionHandler getHandler() {
            return new SaveActionHandler();
        }
    },
    VERIFY("verify"){
        @Override
        public ActionHandler getHandler() {
            return new VerifyActionHandler();
        }
    },
    IS_AVAILABLE("isAvailable"){
        @Override
        public ActionHandler getHandler() {
            return new IsAvailableActionHandler();
        }
    },
    SET_LOCALE("setLocale"){
        @Override
        public ActionHandler getHandler() {
            return new SetLocaleActionHandler();
        }
    },
    HAS("has"){
        @Override
        public ActionHandler getHandler() {
            return new HasActionHandler();
        }
    },
    DELETE("delete"){
        @Override
        public ActionHandler getHandler() {
            return new DeleteActionHandler();
        }
    };

    private final String value;

    @Nullable
    public static Action getOrNull(String value) {
        for (Action action : Action.values()) {
            if (action.value.equals(value)) {
                return action;
            }
        }
        return null;
    }

    Action(String value) {
        this.value = value;
    }

    public abstract ActionHandler getHandler();
}
