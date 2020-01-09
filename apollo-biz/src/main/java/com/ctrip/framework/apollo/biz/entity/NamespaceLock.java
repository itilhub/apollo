package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;

import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * NamespaceLock 命名空间锁 实体
 * 悲观锁：默认关闭
 * 写操作 Item 时，创建 Namespace 对应的 NamespaceLock 记录到 ConfigDB 数据库中，从而记录配置修改人
 */
@Entity
@Table(name = "NamespaceLock")
@Where(clause = "isDeleted = 0")
public class NamespaceLock extends BaseEntity{

  /**
   * namespace 编号
   * 该字段上有唯一索引。通过该锁定，保证并发写操作时，同一个 Namespace 有且仅有创建一条 NamespaceLock 记录。
   */
  @Column(name = "NamespaceId")
  private long namespaceId;

  public long getNamespaceId() {
    return namespaceId;
  }

  public void setNamespaceId(long namespaceId) {
    this.namespaceId = namespaceId;
  }
}
