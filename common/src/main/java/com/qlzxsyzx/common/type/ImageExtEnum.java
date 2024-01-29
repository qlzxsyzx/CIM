package com.qlzxsyzx.common.type;

import lombok.Data;

public enum ImageExtEnum {
    IMAGE_JPEG("jpeg"),
    IMAGE_PNG("png"),
    IMAGE_GIF("gif"),
    IMAGE_JPG("jpg"),
    IMAGE_WEBP("webp");

    private final String ext;

    ImageExtEnum(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public static boolean isImageExt(String ext) {
        for (ImageExtEnum imageExtEnum : ImageExtEnum.values()) {
            if (imageExtEnum.getExt().equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
}
