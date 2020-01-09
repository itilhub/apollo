package com.ctrip.framework.apollo.portal.api;


import com.ctrip.framework.apollo.portal.component.RetryableRestTemplate;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 封装Api 抽象类
 */
public abstract class API {

  /**
   * 提供统一的 restTemplate 的属性注入
   */
  @Autowired
  protected RetryableRestTemplate restTemplate;

}
