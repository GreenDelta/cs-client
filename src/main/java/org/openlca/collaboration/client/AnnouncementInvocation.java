package org.openlca.collaboration.client;

import org.openlca.collaboration.client.WebRequests.Type;

class AnnouncementInvocation extends Invocation<Announcement, Announcement> {

	AnnouncementInvocation() {
		super(Type.GET, "public/announcements", Announcement.class);
	}

}
