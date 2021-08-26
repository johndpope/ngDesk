package com.ngdesk.repositories.channels;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.channels.email.dao.Channel;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface EmailChannelRepository extends CustomNgdeskRepository<Channel, String> {

}
