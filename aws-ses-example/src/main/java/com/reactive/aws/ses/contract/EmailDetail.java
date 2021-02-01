package com.reactive.aws.ses.contract;

import lombok.Data;

/**
 * The Email Detail Info.
 */
@Data
public class EmailDetail {

    /**
     * The target email address.
     */
    private String email;

    /**
     * The email title.
     */
    private String title;

    /**
     * The email subTitle.
     */
    private String subTitle;

    /**
     * The email html content.
     */
    private String content;
}
