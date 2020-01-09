package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.repository.NamespaceLockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * NamespaceLock 悲观锁
 */
@Service
public class NamespaceLockService {

  private final NamespaceLockRepository namespaceLockRepository;

  public NamespaceLockService(final NamespaceLockRepository namespaceLockRepository) {
    this.namespaceLockRepository = namespaceLockRepository;
  }

  /**
   * 寻找锁
   * @param namespaceId
   * @return
   */
  public NamespaceLock findLock(Long namespaceId){
    return namespaceLockRepository.findByNamespaceId(namespaceId);
  }


  /**
   * 尝试琐
   * @param lock
   * @return
   */
  @Transactional
  public NamespaceLock tryLock(NamespaceLock lock){
    return namespaceLockRepository.save(lock);
  }

  /**
   * 解锁
   * @param namespaceId
   */
  @Transactional
  public void unlock(Long namespaceId){
    namespaceLockRepository.deleteByNamespaceId(namespaceId);
  }
}
