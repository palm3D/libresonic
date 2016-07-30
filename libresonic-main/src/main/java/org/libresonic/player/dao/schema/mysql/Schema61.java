/*
 This file is part of Libresonic.

 Libresonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Libresonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Libresonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2016 (C) Libresonic Authors
 Based upon Subsonic, Copyright 2009 (C) Sindre Mehus
 */
package org.libresonic.player.dao.schema.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.libresonic.player.Logger;
import org.libresonic.player.dao.schema.Schema;
import org.libresonic.player.util.Util;
import org.libresonic.player.domain.TranscodeScheme;
import java.util.Arrays;
import org.libresonic.player.domain.AlbumListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.apache.commons.io.IOUtils;

/**
 * Used for creating and evolving the database schema.
 * This class implementes the database schema for Libresonic version 6.1.
 *
 * @author Bernardus Jansen
 * Based on hsql schemas by Sindre Mehus
 */
public class Schema61 extends Schema {
    private static final Logger LOG = Logger.getLogger(Schema61.class);

    private static final String[] AVATARS = {
            "Formal", "Engineer", "Footballer", "Green-Boy",

            "Linux-Zealot", "Mac-Zealot", "Windows-Zealot", "Army-Officer", "Beatnik",
            "All-Caps", "Clown", "Commie-Pinko", "Forum-Flirt", "Gamer", "Hopelessly-Addicted",
            "Jekyll-And-Hyde", "Joker", "Lurker", "Moderator", "Newbie", "No-Dissent",
            "Performer", "Push-My-Button", "Ray-Of-Sunshine", "Red-Hot-Chili-Peppers-1",
            "Red-Hot-Chili-Peppers-2", "Red-Hot-Chili-Peppers-3", "Red-Hot-Chili-Peppers-4",
            "Ringmaster", "Rumor-Junkie", "Sozzled-Surfer", "Statistician", "Tech-Support",
            "The-Guru", "The-Referee", "Troll", "Uptight",

            "Fire-Guitar", "Drum", "Headphones", "Mic", "Turntable", "Vinyl",

            "Cool", "Laugh", "Study"
    };

    public void execute(JdbcTemplate template) {
        template.execute("SET GLOBAL innodb_file_format = BARRACUDA");
        template.execute("SET GLOBAL innodb_large_prefix = ON");
        
        if (!tableExists(template, "version")) {

            LOG.info("Database table 'version' not found.  Creating it.");
            template.execute("CREATE TABLE version (version INT NOT NULL)");
            template.execute("INSERT INTO version VALUES (1)");
            LOG.info("Database table 'version' was created successfully.");
        }

        if (!tableExists(template, "role")) {
            LOG.info("Database table 'role' not found.  Creating it.");
            template.execute("CREATE TABLE role (" +
                             "id INT NOT NULL," +
                             "name VARCHAR(64) NOT NULL," +
                             "PRIMARY KEY (id))");
            template.execute("INSERT INTO role VALUES (1, 'admin')");
            template.execute("INSERT INTO role VALUES (2, 'download')");
            template.execute("INSERT INTO role VALUES (3, 'upload')");
            template.execute("INSERT INTO role VALUES (4, 'playlist')");
            template.execute("INSERT INTO role VALUES (5, 'coverart')");
            template.execute("INSERT INTO role VALUES (6, 'comment')");
            template.execute("INSERT INTO role VALUES (7, 'podcast')");
            template.execute("INSERT INTO role VALUES (8, 'stream')");
            template.execute("INSERT INTO role VALUES (9, 'settings')");
            template.execute("INSERT INTO role VALUES (10, 'jukebox')");
            template.execute("INSERT INTO role VALUES (11, 'share')");
            LOG.info("Database table 'role' was created successfully.");
        }

        if (!tableExists(template, "user")) {
            LOG.info("Database table 'user' not found.  Creating it.");
            template.execute("CREATE TABLE user (" +
                             "username VARCHAR(64) NOT NULL," +
                             "password VARCHAR(191) NOT NULL," +
                             "bytes_streamed BIGINT DEFAULT 0 NOT NULL," +
                             "bytes_downloaded BIGINT DEFAULT 0 NOT NULL," +
                             "bytes_uploaded BIGINT DEFAULT 0 NOT NULL," +
                             "ldap_authenticated BOOLEAN DEFAULT 0 NOT NULL," +
                             "email VARCHAR(191)," +
                             "PRIMARY KEY (username))");
            //create admin user
            template.execute("INSERT INTO user VALUES ('admin', 'admin', 0, 0, 0 ,0, '')");
            LOG.info("Database table 'user' was created successfully.");
        }

        if (!tableExists(template, "user_role")) {
            LOG.info("Database table 'user_role' not found.  Creating it.");
            template.execute("CREATE TABLE user_role (" +
                             "username VARCHAR(64) NOT NULL," +
                             "role_id INT NOT NULL," +
                             "PRIMARY KEY (username, role_id)," +
                             "FOREIGN KEY (username) REFERENCES user(username)," +
                             "FOREIGN KEY (role_id) REFERENCES role(id))");
            template.execute("INSERT INTO user_role VALUES ('admin', 1)");
            template.execute("INSERT INTO user_role VALUES ('admin', 2)");
            template.execute("INSERT INTO user_role VALUES ('admin', 3)");
            template.execute("INSERT INTO user_role VALUES ('admin', 4)");
            template.execute("INSERT INTO user_role VALUES ('admin', 5)");
            template.execute("INSERT INTO user_role " +
                             "SELECT DISTINCT u.username, 6 FROM user u, user_role ur " +
                             "WHERE u.username = ur.username AND ur.role_id IN (1, 5)");
            template.execute("INSERT INTO user_role " +
                             "SELECT DISTINCT u.username, 7 FROM user u, user_role ur " +
                             "WHERE u.username = ur.username AND ur.role_id = 1");
            template.execute("INSERT INTO user_role SELECT DISTINCT u.username, 8 FROM user u");
            template.execute("INSERT INTO user_role SELECT DISTINCT u.username, 9 FROM user u");
            template.execute("INSERT INTO user_role " +
                             "SELECT DISTINCT u.username, 10 FROM user u, user_role ur " +
                             "WHERE u.username = ur.username and ur.role_id = 1");
            template.execute("INSERT INTO user_role " +
                             "SELECT DISTINCT u.username, 11 FROM user u, user_role ur " +
                             "WHERE u.username = ur.username and ur.role_id = 1");
            LOG.info("Database table 'user_role' was created successfully.");
        }

        if (!tableExists(template, "music_folder")) {
            LOG.info("Database table 'music_folder' not found.  Creating it.");
            template.execute("CREATE TABLE music_folder (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "path VARCHAR(500) NOT NULL," +
                             "name VARCHAR(255) NOT NULL," +
                             "enabled BOOLEAN NOT NULL," +
                             "changed DATETIME DEFAULT 0 NOT NULL," +
                             "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");
            template.execute("INSERT INTO music_folder VALUES (null, '" + Util.getDefaultMusicFolder() + "', 'Music', 1, 0)");
            LOG.info("Database table 'music_folder' was created successfully.");
        }

        if (!tableExists(template, "internet_radio")) {
            LOG.info("Database table 'internet_radio' not found.  Creating it.");
            template.execute("CREATE TABLE internet_radio (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "name VARCHAR(191) NOT NULL," +
                             "stream_url VARCHAR(500) NOT NULL," +
                             "homepage_url VARCHAR(500)," +
                             "enabled BOOLEAN NOT NULL," +
                             "changed DATETIME default 0 NOT NULL," +
                             "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");
            LOG.info("Database table 'internet_radio' was created successfully.");
        }

        if (!tableExists(template, "player")) {
            LOG.info("Database table 'player' not found.  Creating it.");
            template.execute("CREATE TABLE player (" +
                             "id INT NOT NULL," +
                             "name VARCHAR(64)," +
                             "type VARCHAR(191)," +
                             "username VARCHAR(64)," +
                             "ip_address VARCHAR(15)," +
                             "auto_control_enabled BOOLEAN NOT NULL," +
                             "last_seen DATETIME," +
                             "cover_art_scheme VARCHAR(191) NOT NULL," +
                             "transcode_scheme VARCHAR(191) default '" + TranscodeScheme.OFF.name() + "' NOT NULL," +
                             "dynamic_ip BOOLEAN DEFAULT 1 NOT NULL," +
                             "client_side_playlist BOOLEAN DEFAULT 0 NOT NULL," +
                             "jukebox BOOLEAN DEFAULT 0 NOT NULL," +
                             "technology VARCHAR(32) DEFAULT 'WEB' NOT NULL," +
                             "client_id VARCHAR(191)," +
                             "PRIMARY KEY (id))");
            LOG.info("Database table 'player' was created successfully.");

        }

        if (!tableExists(template, "system_avatar")) {
            LOG.info("Database table 'system_avatar' not found.  Creating it.");
            template.execute("CREATE TABLE system_avatar (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "name VARCHAR(64)," +
                             "created_date DATETIME NOT NULL," +
                             "mime_type VARCHAR(191) NOT NULL," +
                             "width INT NOT NULL," +
                             "height INT NOT NULL," +
                             "data BLOB NOT NULL," +
                             "PRIMARY KEY (id))");
            LOG.info("Database table 'system_avatar' was created successfully.");
        }

        for (String avatar : AVATARS) {
            createAvatar(template, avatar);
        }

        if (!tableExists(template, "user_settings")) {
            LOG.info("Database table 'user_settings' not found.  Creating it.");
            //divided in multiple queries due to length
            template.execute("CREATE TABLE user_settings (" +
                             "username VARCHAR(64) NOT NULL," +
                             "locale VARCHAR(32)," +
                             "theme_id VARCHAR(64)," +
                             "final_version_notification BOOLEAN DEFAULT 1 NOT NULL," +
                             "beta_version_notification BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_caption_cutoff INT DEFAULT 35 NOT NULL," +
                             "main_track_number BOOLEAN DEFAULT 1 NOT NULL," +
                             "main_artist BOOLEAN DEFAULT 1 NOT NULL," +
                             "main_album BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_genre BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_year BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_bit_rate BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_duration BOOLEAN DEFAULT 1 NOT NULL," +
                             "main_format BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_file_size BOOLEAN DEFAULT 0 NOT NULL," +
                             "playlist_caption_cutoff INT DEFAULT 35 NOT NULL," +
                             "playlist_track_number BOOLEAN DEFAULT 0 NOT NULL," +
                             "playlist_artist BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_album BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_genre BOOLEAN DEFAULT 0 NOT NULL," +
                             "playlist_year BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_bit_rate BOOLEAN DEFAULT 0 NOT NULL," +
                             "playlist_duration BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_format BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_file_size BOOLEAN DEFAULT 1 NOT NULL," +
                             "last_fm_enabled BOOLEAN DEFAULT 0 NOT NULL," +
                             "last_fm_username VARCHAR(64) NULL," +
                             "last_fm_password VARCHAR(191) NULL," +
                             "transcode_scheme VARCHAR(500) default '" + TranscodeScheme.OFF.name() + "' NOT NULL," +
                             "show_now_playing BOOLEAN DEFAULT 1 NOT NULL," +
                             "selected_music_folder_id INT DEFAULT -1 NOT NULL," +
                             "party_mode_enabled BOOLEAN DEFAULT 0 NOT NULL," +
                             "now_playing_allowed BOOLEAN DEFAULT 1 NOT NULL," +
                             "avatar_scheme VARCHAR(191) default 'NONE' NOT NULL," +
                             "system_avatar_id INT," +
                             "FOREIGN KEY (system_avatar_id) REFERENCES system_avatar(id)," +
                             "changed DATETIME default 0 NOT NULL," +
                             "show_chat BOOLEAN DEFAULT 1 NOT NULL," +
                             "song_notification BOOLEAN default 1 NOT NULL," +
                             "show_artist_info BOOLEAN default 1 NOT NULL," +
                             "auto_hide_play_queue BOOLEAN default 1 NOT NULL," +
                             "view_as_list BOOLEAN default 0 NOT NULL," +
                             "default_album_list VARCHAR(191) default '" + AlbumListType.RANDOM.getId() + "' NOT NULL," +
                             "queue_following_songs BOOLEAN DEFAULT 1 NOT NULL," +
                             "show_side_bar BOOLEAN DEFAULT 1 NOT NULL," +
                             "PRIMARY KEY (username)," +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE)");

            LOG.info("Database table 'user_settings' was created successfully.");
        }

        if (!tableExists(template, "transcoding")) {
            LOG.info("Database table 'transcoding' not found.  Creating it.");
            template.execute("CREATE TABLE transcoding (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "name VARCHAR(64) NOT NULL," +
                             "source_format VARCHAR(16) NOT NULL," +
                             "target_format VARCHAR(16) NOT NULL," +
                             "step1 VARCHAR(500) NOT NULL," +
                             "step2 VARCHAR(500)," +
                             "step3 VARCHAR(500)," +
                             "enabled BOOLEAN NOT NULL," +
                             "default_active BOOLEAN DEFAULT 1 NOT NULL," +
                             "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");

            LOG.info("Database table 'transcoding' was created successfully.");
        }

        if (!tableExists(template, "player_transcoding")) {
            LOG.info("Database table 'player_transcoding' not found.  Creating it.");
            template.execute("CREATE TABLE player_transcoding (" +
                             "player_id INT NOT NULL," +
                             "transcoding_id INT NOT NULL," +
                             "PRIMARY KEY (player_id, transcoding_id)," +
                             "FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE," +
                             "FOREIGN KEY (transcoding_id) REFERENCES transcoding(id) ON DELETE CASCADE)");

            for (String format : Arrays.asList("avi", "mpg", "mpeg", "mp4", "m4v", "mkv", "mov", "wmv", "ogv")) {
                  template.update("delete FROM transcoding WHERE source_format=? and target_format=?", new Object[] {format, "flv"});
                  template.execute("INSERT INTO transcoding VALUES(null,'" + format + " > flv' ,'" + format + "' ,'flv','ffmpeg -ss %o -i %s -async 1 -b %bk -s %wx%h -ar 44100 -ac 2 -v 0 -f flv -',null,null,1,1)");
                  template.execute("INSERT INTO player_transcoding SELECT p.id as player_id, t.id as transaction_id FROM player p, transcoding t WHERE t.name = '" + format + " > flv'");
            }
            LOG.info("Database table 'player_transcoding' was created successfully.");
        }

        if (!tableExists(template, "user_rating")) {
            LOG.info("Database table 'user_rating' not found.  Creating it.");
            template.execute("CREATE TABLE user_rating (" +
                             "username VARCHAR(64) NOT NULL," +
                             "path VARCHAR(500) NOT NULL," +
                             "rating DOUBLE NOT NULL," +
                             "PRIMARY KEY (username, path)," +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE) ROW_FORMAT = DYNAMIC");
            LOG.info("Database table 'user_rating' was created successfully.");
        }

        if (!tableExists(template, "podcast_channel")) {
            LOG.info("Database table 'podcast_channel' not found.  Creating it.");
            template.execute("CREATE TABLE podcast_channel (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "url VARCHAR(500) NOT NULL," +
                             "title VARCHAR(191)," +
                             "description VARCHAR(500)," +
                             "status VARCHAR(191) NOT NULL," +
                             "error_message VARCHAR(500)," +
                             "image_url VARCHAR(500)," +
                             "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");
            LOG.info("Database table 'podcast_channel' was created successfully.");
        }

        if (!tableExists(template, "podcast_episode")) {
            LOG.info("Database table 'podcast_episode' not found.  Creating it.");
            template.execute("CREATE TABLE podcast_episode (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "channel_id INT NOT NULL," +
                             "url VARCHAR(500) NOT NULL," +
                             "path VARCHAR(500)," +
                             "title VARCHAR(191)," +
                             "description VARCHAR(500)," +
                             "publish_date DATETIME," +
                             "duration VARCHAR(191)," +
                             "bytes_total BIGINT," +
                             "bytes_downloaded BIGINT," +
                             "status VARCHAR(191) NOT NULL," +
                             "error_message VARCHAR(500)," +
                             "PRIMARY KEY (id)," +
                             "FOREIGN KEY (channel_id) REFERENCES podcast_channel(id) ON DELETE CASCADE) ROW_FORMAT = DYNAMIC");
            LOG.info("Database table 'podcast_episode' was created successfully.");

            if (!rowExists(template, "table_name='PODCAST_EPISODE' and column_name='URL' and ordinal_position=1",
                           "information_schema.system_indexinfo")) {
                template.execute("CREATE INDEX idx_podcast_episode_url on podcast_episode(url)");
                LOG.info("Created index for podcast_episode.url");
            }
        }

        if (!tableExists(template, "custom_avatar")) {
            LOG.info("Database table 'custom_avatar' not found.  Creating it.");
            template.execute("CREATE TABLE custom_avatar (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "name VARCHAR(64)," +
                             "created_date DATETIME NOT NULL," +
                             "mime_type VARCHAR(191) NOT NULL," +
                             "width INT NOT NULL," +
                             "height INT NOT NULL," +
                             "data BLOB NOT NULL," +
                             "username VARCHAR(64) NOT NULL," +
                             "PRIMARY KEY (id)," +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE)");
            LOG.info("Database table 'custom_avatar' was created successfully.");
        }

        if (!tableExists(template, "share")) {
            LOG.info("Table 'share' not found in database. Creating it.");
            template.execute("CREATE TABLE share (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "name VARCHAR(64) NOT NULL," +
                    "description VARCHAR(191)," +
                    "username VARCHAR(64) NOT NULL," +
                    "created DATETIME NOT NULL," +
                    "expires DATETIME," +
                    "last_visited DATETIME," +
                    "visit_count INT DEFAULT 0 NOT NULL," +
                    "unique (name)," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE)");
            template.execute("CREATE INDEX idx_share_name on share(name)");

            LOG.info("Table 'share' was created successfully.");
            LOG.info("Table 'share_file' not found in database. Creating it.");
            template.execute("CREATE TABLE share_file (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "share_id INT NOT NULL," +
                    "path VARCHAR(500) NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (share_id) REFERENCES share(id) ON DELETE CASCADE) ROW_FORMAT = DYNAMIC");
            LOG.info("Table 'share_file' was created successfully.");
        }

        if (!tableExists(template, "transcoding2")) {
            LOG.info("Database table 'transcoding2' not found.  Creating it.");
            template.execute("CREATE TABLE transcoding2 (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "name VARCHAR(64) NOT NULL," +
                             "source_formats VARCHAR(191) NOT NULL," +
                             "target_format VARCHAR(191) NOT NULL," +
                             "step1 VARCHAR(500) NOT NULL," +
                             "step2 VARCHAR(500)," +
                             "step3 VARCHAR(500)," +
                             "default_active BOOLEAN DEFAULT 1 NOT NULL," +
                             "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");

            template.execute("INSERT INTO transcoding2(name, source_formats, target_format, step1) VALUES('mp3 audio'," +
                    "'ogg oga aac m4a flac wav wma aif aiff ape mpc shn', 'mp3', " +
                    "'ffmpeg -i %s -ab %bk -v 0 -f mp3 -')");

            template.execute("INSERT INTO transcoding2(name, source_formats, target_format, step1) VALUES('flv/h264 video', " +
                    "'avi mpg mpeg mp4 m4v mkv mov wmv ogv divx m2ts', 'flv', " +
                    "'ffmpeg -ss %o -i %s -async 1 -b %bk -s %wx%h -ar 44100 -ac 2 -v 0 -f flv -vcodec libx264 -preset superfast -threads 0 -')");

            template.execute("INSERT INTO transcoding2(name, source_formats, target_format, step1, default_active) VALUES('mkv video', " +
                    "'avi mpg mpeg mp4 m4v mkv mov wmv ogv divx m2ts', 'mkv', " +
                    "'ffmpeg -ss %o -i %s -c:v libx264 -preset superfast -b:v %bk -c:a libvorbis -f matroska -threads 0 -', 1)");

            LOG.info("Added mkv transcoding.");
            template.execute("update transcoding2 set step1='ffmpeg -i %s -map 0:0 -b:a %bk -v 0 -f mp3 -' WHERE name='mp3 audio'");

            LOG.info("Database table 'transcoding2' was created successfully.");
        }

        if (!tableExists(template, "player_transcoding2")) {
            LOG.info("Database table 'player_transcoding2' not found.  Creating it.");
            template.execute("CREATE TABLE player_transcoding2 (" +
                             "player_id INT NOT NULL," +
                             "transcoding_id INT NOT NULL," +
                             "PRIMARY KEY (player_id, transcoding_id)," +
                             "FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE," +
                             "FOREIGN KEY (transcoding_id) REFERENCES transcoding2(id) ON DELETE CASCADE)");

            template.execute("INSERT INTO player_transcoding2(player_id, transcoding_id) " +
                    "SELECT DISTINCT p.id, t.id FROM player p, transcoding2 t");

            template.execute("INSERT INTO player_transcoding2(player_id, transcoding_id) " +
                    "SELECT DISTINCT p.id, t.id FROM player p, transcoding2 t WHERE t.name='mkv video'");

            LOG.info("Database table 'player_transcoding2' was created successfully.");
        }

        if (!tableExists(template, "media_file")) {
            LOG.info("Database table 'media_file' not found.  Creating it.");
            template.execute("CREATE TABLE media_file (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "path VARCHAR(500) NOT NULL," +
                    "folder VARCHAR(191)," +
                    "type VARCHAR(191) NOT NULL," +
                    "format VARCHAR(191)," +
                    "title VARCHAR(500)," +
                    "album VARCHAR(191)," +
                    "artist VARCHAR(191)," +
                    "album_artist VARCHAR(191)," +
                    "disc_number INT," +
                    "track_number INT," +
                    "year INT," +
                    "genre VARCHAR(191)," +
                    "bit_rate INT," +
                    "variable_bit_rate BOOLEAN NOT NULL," +
                    "duration_seconds INT," +
                    "file_size BIGINT," +
                    "width INT," +
                    "height INT," +
                    "cover_art_path VARCHAR(500)," +
                    "parent_path VARCHAR(500)," +
                    "play_count INT NOT NULL," +
                    "last_played DATETIME," +
                    "comment VARCHAR(500)," +
                    "created DATETIME NOT NULL," +
                    "changed DATETIME NOT NULL," +
                    "last_scanned DATETIME NOT NULL," +
                    "children_last_updated DATETIME NOT NULL," +
                    "present BOOLEAN NOT NULL," +
                    "version INT NOT NULL," +
                    "unique (path)," +
                    "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");

            template.execute("CREATE INDEX idx_media_file_path on media_file(path)");
            template.execute("CREATE INDEX idx_media_file_parent_path on media_file(parent_path)");
            template.execute("CREATE INDEX idx_media_file_type on media_file(type)");
            template.execute("CREATE INDEX idx_media_file_album on media_file(album)");
            template.execute("CREATE INDEX idx_media_file_artist on media_file(artist)");
            template.execute("CREATE INDEX idx_media_file_album_artist on media_file(album_artist)");
            template.execute("CREATE INDEX idx_media_file_present on media_file(present)");
            template.execute("CREATE INDEX idx_media_file_genre on media_file(genre)");
            template.execute("CREATE INDEX idx_media_file_play_count on media_file(play_count)");
            template.execute("CREATE INDEX idx_media_file_created on media_file(created)");
            template.execute("CREATE INDEX idx_media_file_last_played on media_file(last_played)");

            LOG.info("Database table 'media_file' was created successfully.");
        }

        if (!tableExists(template, "artist")) {
            LOG.info("Database table 'artist' not found.  Creating it.");
            template.execute("CREATE TABLE artist (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "name VARCHAR(191) NOT NULL," +
                    "cover_art_path VARCHAR(500)," +
                    "album_count INT DEFAULT 0 NOT NULL," +
                    "last_scanned DATETIME NOT NULL," +
                    "present BOOLEAN NOT NULL," +
                    "folder_id INT," +
                    "unique (name)," +
                    "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");

            template.execute("CREATE INDEX idx_artist_name ON artist(name)");
            template.execute("CREATE INDEX idx_artist_present ON artist(present)");

            LOG.info("Database table 'artist' was created successfully.");
        }

        if (!tableExists(template, "album")) {
            LOG.info("Database table 'album' not found.  Creating it.");
            template.execute("CREATE TABLE album (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "path VARCHAR(500) NOT NULL," +
                    "name VARCHAR(191) NOT NULL," +
                    "artist VARCHAR(191) NOT NULL," +
                    "song_count INT DEFAULT 0 NOT NULL," +
                    "duration_seconds INT DEFAULT 0 NOT NULL," +
                    "cover_art_path VARCHAR(500)," +
                    "play_count INT DEFAULT 0 NOT NULL," +
                    "last_played DATETIME," +
                    "comment VARCHAR(500)," +
                    "created DATETIME NOT NULL," +
                    "last_scanned DATETIME NOT NULL," +
                    "present BOOLEAN NOT NULL," +
                    "unique (artist, name)," +
                    "year INT," +
                    "genre VARCHAR(191)," +
                    "folder_id INT," +
                    "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");

            template.execute("CREATE INDEX idx_album_artist_name on album(artist, name)");
            template.execute("CREATE INDEX idx_album_play_count on album(play_count)");
            template.execute("CREATE INDEX idx_album_last_played on album(last_played)");
            template.execute("CREATE INDEX idx_album_present on album(present)");
            template.execute("CREATE INDEX idx_album_name on album(name)");

            LOG.info("Database table 'album' was created successfully.");
        }

        if (!tableExists(template, "starred_media_file")) {
            LOG.info("Database table 'starred_media_file' not found.  Creating it.");
            template.execute("CREATE TABLE starred_media_file (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "media_file_id INT NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "created DATETIME NOT NULL," +
                    "FOREIGN KEY (media_file_id) REFERENCES media_file(id) ON DELETE CASCADE,"+
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE," +
                    "unique (media_file_id, username)," +
                    "PRIMARY KEY (id))");

            template.execute("CREATE INDEX idx_starred_media_file_media_file_id on starred_media_file(media_file_id)");
            template.execute("CREATE INDEX idx_starred_media_file_username on starred_media_file(username)");

            LOG.info("Database table 'starred_media_file' was created successfully.");
        }

        if (!tableExists(template, "starred_album")) {
            LOG.info("Database table 'starred_album' not found.  Creating it.");
            template.execute("CREATE TABLE starred_album (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "album_id INT NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "created DATETIME NOT NULL," +
                    "FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE," +
                    "unique (album_id, username)," +
                    "PRIMARY KEY (id))");

            template.execute("CREATE INDEX idx_starred_album_album_id on starred_album(album_id)");
            template.execute("CREATE INDEX idx_starred_album_username on starred_album(username)");

            LOG.info("Database table 'starred_album' was created successfully.");
        }

        if (!tableExists(template, "starred_artist")) {
            LOG.info("Database table 'starred_artist' not found.  Creating it.");
            template.execute("CREATE TABLE starred_artist (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "artist_id INT NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "created DATETIME NOT NULL," +
                    "FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE,"+
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE," +
                    "unique (artist_id, username)," +
                    "PRIMARY KEY (id))");

            template.execute("CREATE INDEX idx_starred_artist_artist_id on starred_artist(artist_id)");
            template.execute("CREATE INDEX idx_starred_artist_username on starred_artist(username)");

            LOG.info("Database table 'starred_artist' was created successfully.");
        }

        if (!tableExists(template, "playlist")) {
            LOG.info("Database table 'playlist' not found.  Creating it.");
            template.execute("CREATE TABLE playlist (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "username VARCHAR(64) NOT NULL," +
                    "is_public BOOLEAN NOT NULL," +
                    "name VARCHAR(191) NOT NULL," +
                    "comment VARCHAR(500)," +
                    "file_count INT DEFAULT 0 NOT NULL," +
                    "duration_seconds INT DEFAULT 0 NOT NULL," +
                    "created DATETIME NOT NULL," +
                    "changed DATETIME NOT NULL," +
                    "imported_FROM VARCHAR(191)," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE) ROW_FORMAT = DYNAMIC");

            LOG.info("Database table 'playlist' was created successfully.");
        }

        if (!tableExists(template, "playlist_file")) {
            LOG.info("Database table 'playlist_file' not found.  Creating it.");
            template.execute("CREATE TABLE playlist_file (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "playlist_id INT NOT NULL," +
                    "media_file_id INT NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (media_file_id) REFERENCES media_file(id) ON DELETE CASCADE)");

            LOG.info("Database table 'playlist_file' was created successfully.");
        }

        if (!tableExists(template, "playlist_user")) {
            LOG.info("Database table 'playlist_user' not found.  Creating it.");
            template.execute("CREATE TABLE playlist_user (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "playlist_id INT NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "unique(playlist_id, username)," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE)");

            LOG.info("Database table 'playlist_user' was created successfully.");
        }

        if (!tableExists(template, "bookmark")) {
            LOG.info("Database table 'bookmark' not found.  Creating it.");
            template.execute("CREATE TABLE bookmark (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "media_file_id INT NOT NULL," +
                    "position_millis BIGINT NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "comment VARCHAR(500)," +
                    "created DATETIME NOT NULL," +
                    "changed DATETIME NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (media_file_id) REFERENCES media_file(id) ON DELETE CASCADE,"+
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE," +
                    "unique (media_file_id, username)) ROW_FORMAT = DYNAMIC");

            template.execute("CREATE INDEX idx_bookmark_media_file_id on bookmark(media_file_id)");
            template.execute("CREATE INDEX idx_bookmark_username on bookmark(username)");

            LOG.info("Database table 'bookmark' was created successfully.");
        }

        if (!tableExists(template, "genre")) {
            LOG.info("Database table 'genre' not found.  Creating it.");
            template.execute("CREATE TABLE genre (" +
                    "name VARCHAR(191) NOT NULL," +
                    "song_count INT NOT NULL," +
                    "album_count INT DEFAULT 0 NOT NULL)");

            LOG.info("Database table 'genre' was created successfully.");
        }

        if (!tableExists(template, "music_folder_user")) {
            LOG.info("Database table 'music_folder_user' not found.  Creating it.");
            template.execute("CREATE TABLE music_folder_user (" +
                             "music_folder_id INT NOT NULL," +
                             "username VARCHAR(64) NOT NULL, " +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE, " +
                             "FOREIGN KEY (music_folder_id) REFERENCES music_folder(id) ON DELETE CASCADE)");
            template.execute("CREATE INDEX idx_music_folder_user_username on music_folder_user(username)");
            template.execute("INSERT INTO music_folder_user SELECT music_folder.id, user.username FROM music_folder, user");
            LOG.info("Database table 'music_folder_user' was created successfully.");
        }

        if (!tableExists(template, "play_queue")) {
            LOG.info("Database table 'play_queue' not found.  Creating it.");
            template.execute("CREATE TABLE play_queue (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "username VARCHAR(64) NOT NULL," +
                             "current INT," +
                             "position_millis BIGINT," +
                             "changed DATETIME NOT NULL," +
                             "changed_by VARCHAR(191) NOT NULL," +
                             "PRIMARY KEY (id)," +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE)");
            LOG.info("Database table 'play_queue' was created successfully.");
        }

        if (!tableExists(template, "play_queue_file")) {
            LOG.info("Database table 'play_queue_file' not found.  Creating it.");
            template.execute("CREATE TABLE play_queue_file (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "play_queue_id INT NOT NULL," +
                             "media_file_id INT NOT NULL," +
                             "PRIMARY KEY (id)," +
                             "FOREIGN KEY (play_queue_id) REFERENCES play_queue(id) ON DELETE CASCADE," +
                             "FOREIGN KEY (media_file_id) REFERENCES media_file(id) ON DELETE CASCADE)");

            LOG.info("Database table 'play_queue_file' was created successfully.");
        }
    }

    private void createAvatar(JdbcTemplate template, String avatar) {
        if (template.queryForInt("SELECT count(*) FROM system_avatar WHERE name = ?", new Object[]{avatar}) == 0) {

            InputStream in = null;
            try {
                in = getClass().getResourceAsStream("/org/libresonic/player/dao/schema/" + avatar + ".png");
                byte[] imageData = IOUtils.toByteArray(in);
                template.update("INSERT INTO system_avatar VALUES (null, ?, ?, ?, ?, ?, ?)",
                                new Object[]{avatar, new Date(), "image/png", 48, 48, imageData});
                LOG.info("Created avatar '" + avatar + "'.");
            } catch (IOException x) {
                LOG.error("Failed to create avatar '" + avatar + "'.", x);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }
}
