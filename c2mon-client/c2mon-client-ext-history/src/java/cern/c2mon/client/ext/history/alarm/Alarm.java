package cern.c2mon.client.ext.history.alarm;


import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Entity bean for historical alarm values.
 *
 * @author Justin Lewis Salmon
 */
@Entity
@Data
@Table(name = "alarmlog")
public class Alarm {

  @Id
  @Column(name = "servertime")
  private Timestamp timestamp;

  @Column(name = "alarmid")
  private Long id;

  @Column(name = "tagid")
  private Long tagId;

  @Column(name = "faultcode")
  private int faultCode;

  @Column(name = "faultfamily")
  private String faultFamily;

  @Column(name = "faultmember")
  private String faultMember;

  @Type(type="yes_no")
  private boolean active;

  private String info;
}
