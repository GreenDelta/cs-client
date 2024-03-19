package org.openlca.collaboration.client;

import org.openlca.collaboration.client.AnnouncementInvocation.Announcement;
import org.openlca.collaboration.client.WebRequests.Type;

/**
 * Invokes a webservice call to check for server announcements
 */
public final class AnnouncementInvocation extends Invocation<Announcement, Announcement> {

	AnnouncementInvocation() {
		super(Type.GET, "public/announcements", Announcement.class);
	}

	public record Announcement(String id, String message) {
	}

}
