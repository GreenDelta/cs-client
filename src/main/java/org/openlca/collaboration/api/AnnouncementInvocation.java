package org.openlca.collaboration.api;

import org.openlca.collaboration.api.AnnouncementInvocation.Announcement;
import org.openlca.collaboration.api.WebRequests.Type;

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
