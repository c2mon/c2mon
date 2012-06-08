package cern.c2mon.notification.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Subscriber {

	/**
	 * the username of the subscriber
	 */
	private final String name;
	/**
	 * the email of the subscriber
	 */
	private final String email;
	/**
	 * the telephone number of the subscriber
	 */
	private final String sms;
	/**
	 * States how often a report should be sent [sec].
	 * 
	 */
	private int reportInterval = 0;

	private HashMap<Long, Subscription> subscriptions = new HashMap<Long, Subscription>();

	/**
	 * Constructor for a subscriber.
	 * 
	 * @param name
	 *            The username of the subscriber (no spaces).
	 * @param email
	 *            The email address.
	 * @param sms
	 *            The telephone number for sms notification.
	 */
	public Subscriber(String name, String email, String sms) {
		this.name = name;
		this.email = email;

		if (sms != null && sms.startsWith("16")) {
			sms = sms.replaceFirst("16", "004176487");
		}
		this.sms = sms;
	}

	/**
	 * Constructor for a subscriber.
	 * 
	 * @param name
	 *            The username of the subscriber (no spaces).
	 * @param email
	 *            The email address.
	 * @param sms
	 *            The telephone number for sms notification.
	 * @param reportInterval
	 *            the interval [hours] for the report interval
	 */
	public Subscriber(String name, String email, String sms, int reportInterval) {
		this(name, email, sms);
		this.reportInterval = reportInterval;
	}

	/**
	 * @return the name
	 */
	public String getUserName() {
		return name;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the sms
	 */
	public String getSms() {
		return sms;
	}

	public HashMap<Long, Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void addSubscription(Subscription subscription) {
		subscriptions.put(subscription.getTagId(), subscription);
	}

	public void removeSubscription(Subscription subscription) {
		removeSubscription(subscription.getTagId());
	}

	/**
	 * Removes a Subscription from {@link Subscriber#subscriptions}. 
	 * @param tagId the id to remove
	 */
	public void removeSubscription(Long tagId) {
		subscriptions.remove(tagId);
	}

	public List<Long> getSubscribedTagIds() {
		return new ArrayList<Long>(subscriptions.keySet());
	}

	/**
	 * Compares two Subscriber objects. They're consider as the same, if the all
	 * fields are equal (string compare results in 0).
	 */
	public final boolean equals(Object obj) {
		if (obj != null && obj instanceof Subscriber) {
			Subscriber s = (Subscriber) obj;
			return getUserName().equals(s.getUserName())
					&& getEmail().equals(s.getEmail());
		}
		return false;
	}

	/**
	 * @param toCompare
	 *            the {@link Subscriber} object we compare to.
	 * @return 0,1,2
	 */
	public int compareTo(final Subscriber toCompare) {
		return this.getUserName().compareTo(toCompare.getUserName());
	}

	/**
	 * @return The report interval [sec]
	 */
	public final int getReportInterval() {
		return reportInterval;
	}

	/**
	 * @param interval
	 *            The new report interval [sec]
	 */
	public void setReportInterval(final int interval) {
		reportInterval = interval;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return a string representation of this object.
	 */
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("UserName=").append(name).append(", Email=").append(email)
				.append(", SMS=").append(sms).append(", ReportInterval=")
				.append(reportInterval)
				.append(", Subscriptions={");
		ArrayList<Subscription> toPrint = new ArrayList<Subscription>(subscriptions.values());
		Collections.sort(toPrint);

		for (Subscription s : subscriptions.values()) {
			ret.append("{").append(s).append("}, ");
		}
		
		return ret.append("}").toString();
	}

	/**
	 * @return an exact copy of this subscriber object.
	 */
    public Subscriber getCopy() {
        Subscriber result = new Subscriber(this.getUserName(), this.getEmail(), this.getSms());
        for (Subscription s : subscriptions.values()) {
            result.addSubscription(s.getCopy());
        }
        return result;
    }

}
