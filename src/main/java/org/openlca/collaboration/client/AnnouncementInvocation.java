package org.openlca.collaboration.client;

import org.openlca.collaboration.client.AnnouncementInvocation.Announcement;
import org.openlca.collaboration.client.WebRequests.Type;

final class AnnouncementInvocation extends Invocation<Announcement, Announcement> {

	AnnouncementInvocation() {
		super(Type.GET, "public/announcements", Announcement.class);
	}

	public record Announcement(String id, String message) {
	}

}
