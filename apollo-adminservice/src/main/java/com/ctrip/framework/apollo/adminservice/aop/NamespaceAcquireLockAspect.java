package com.ctrip.framework.apollo.adminservice.aop;


import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.service.ItemService;
import com.ctrip.framework.apollo.biz.service.NamespaceLockService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;


/**
 * NamespaceLock 切面
 * 一个namespace在一次发布中只能允许一个人修改配置
 * 通过数据库lock表来实现
 *
 * @Aspect 注解，标记为表面类。
 * @Before 注解，标记切入执行方法前。
 */
@Aspect
@Component
public class NamespaceAcquireLockAspect {
  private static final Logger logger = LoggerFactory.getLogger(NamespaceAcquireLockAspect.class);

  private final NamespaceLockService namespaceLockService;
  private final NamespaceService namespaceService;
  private final ItemService itemService;
  private final BizConfig bizConfig;

  public NamespaceAcquireLockAspect(
      final NamespaceLockService namespaceLockService,
      final NamespaceService namespaceService,
      final ItemService itemService,
      final BizConfig bizConfig) {
    this.namespaceLockService = namespaceLockService;
    this.namespaceService = namespaceService;
    this.itemService = itemService;
    this.bizConfig = bizConfig;
  }


  //create item
  @Before("@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, item, ..)")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName,
                                ItemDTO item) {
    // 尝试锁
    acquireLock(appId, clusterName, namespaceName, item.getDataChangeLastModifiedBy());
  }

  //update item
  @Before("@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, itemId, item, ..)")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName, long itemId,
                                ItemDTO item) {
    // 尝试锁
    acquireLock(appId, clusterName, namespaceName, item.getDataChangeLastModifiedBy());
  }

  //update by change set
  @Before("@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, changeSet, ..)")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName,
                                ItemChangeSets changeSet) {
    // 尝试锁
    acquireLock(appId, clusterName, namespaceName, changeSet.getDataChangeLastModifiedBy());
  }

  //delete item
  @Before("@annotation(PreAcquireNamespaceLock) && args(itemId, operator, ..)")
  public void requireLockAdvice(long itemId, String operator) {
    Item item = itemService.findOne(itemId);
    if (item == null){
      throw new BadRequestException("item not exist.");
    }
    // 尝试锁
    acquireLock(item.getNamespaceId(), operator);
  }

  /**
   * 尝试锁
   * @param appId
   * @param clusterName
   * @param namespaceName
   * @param currentUser
   */
  void acquireLock(String appId, String clusterName, String namespaceName,
                           String currentUser) {
    // 判断是否开启悲观锁
    if (bizConfig.isNamespaceLockSwitchOff()) {
      return;
    }

    // 获取锁定对象
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    // 尝试锁定
    acquireLock(namespace, currentUser);
  }

  void acquireLock(long namespaceId, String currentUser) {
    if (bizConfig.isNamespaceLockSwitchOff()) {
      return;
    }

    Namespace namespace = namespaceService.findOne(namespaceId);

    acquireLock(namespace, currentUser);

  }

  private void acquireLock(Namespace namespace, String currentUser) {
    if (namespace == null) {
      throw new BadRequestException("namespace not exist.");
    }

    long namespaceId = namespace.getId();

    // 获取待锁定对象是否已被锁定
    NamespaceLock namespaceLock = namespaceLockService.findLock(namespaceId);
    // 无人锁定
    if (namespaceLock == null) {
      try {
        // 尝试锁定
        tryLock(namespaceId, currentUser);
        //lock success
      } catch (DataIntegrityViolationException e) {
        // 锁定失败
        //lock fail
        namespaceLock = namespaceLockService.findLock(namespaceId);
        // 校验锁定人是否是当前管理员
        checkLock(namespace, namespaceLock, currentUser);
      } catch (Exception e) {
        logger.error("try lock error", e);
        throw e;
      }
    } else {
      //check lock owner is current user
      checkLock(namespace, namespaceLock, currentUser);
    }
  }

  /**
   * 上锁
   * @param namespaceId
   * @param user
   */
  private void tryLock(long namespaceId, String user) {
    NamespaceLock lock = new NamespaceLock();
    lock.setNamespaceId(namespaceId);
    lock.setDataChangeCreatedBy(user);
    lock.setDataChangeLastModifiedBy(user);
    namespaceLockService.tryLock(lock);
  }

  /**
   * 检查锁
   * @param namespace
   * @param namespaceLock
   * @param currentUser
   */
  private void checkLock(Namespace namespace, NamespaceLock namespaceLock,
                         String currentUser) {
    if (namespaceLock == null) {
      throw new ServiceException(
          String.format("Check lock for %s failed, please retry.", namespace.getNamespaceName()));
    }

    // 检查是否被自己锁定
    String lockOwner = namespaceLock.getDataChangeCreatedBy();
    if (!lockOwner.equals(currentUser)) {
      throw new BadRequestException(
          "namespace:" + namespace.getNamespaceName() + " is modified by " + lockOwner);
    }
  }


}
