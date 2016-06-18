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
 * Based on hsql schema's by Sindre Mehus
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
        if (!tableExists(template, "version")) {

            LOG.info("Database table 'version' not found.  Creating it.");
            template.execute("CREATE TABLE version (version int NOT NULL)");
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
            LOG.info("Database table 'role' was created successfully.");
        }

        if (!tableExists(template, "user")) {
            LOG.info("Database table 'user' not found.  Creating it.");
            template.execute("CREATE TABLE user (" +
                             "username VARCHAR(64) NOT NULL," +
                             "password VARCHAR(191) NOT NULL," +
                             "PRIMARY KEY (username))");
            //create admin user
            template.execute("INSERT INTO user VALUES ('admin', 'admin')");
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
            LOG.info("Database table 'user_role' was created successfully.");
        }

        if (!tableExists(template, "music_folder")) {
            LOG.info("Database table 'music_folder' not found.  Creating it.");
            template.execute("CREATE TABLE music_folder (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "path VARCHAR(500) NOT NULL," +
                             "name VARCHAR(255) NOT NULL," +
                             "enabled BOOLEAN NOT NULL," +
                             "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");
            template.execute("INSERT INTO music_folder VALUES (null, '" + Util.getDefaultMusicFolder() + "', 'Music', 1)");
            LOG.info("Database table 'music_folder' was created successfully.");
        }

        if (!tableExists(template, "music_file_info")) {
            LOG.info("Database table 'music_file_info' not found.  Creating it.");
            template.execute("CREATE TABLE music_file_info (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "path VARCHAR(500) NOT NULL," +
                             "rating int," +
                             "comment VARCHAR(500)," +
                             "play_count int," +
                             "last_played datetime," +
                             "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");
            template.execute("create index idx_music_file_info_path on music_file_info(path)");
            LOG.info("Database table 'music_file_info' was created successfully.");
        }

        if (!tableExists(template, "internet_radio")) {
            LOG.info("Database table 'internet_radio' not found.  Creating it.");
            template.execute("CREATE TABLE internet_radio (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "name VARCHAR(191) NOT NULL," +
                             "stream_url VARCHAR(500) NOT NULL," +
                             "homepage_url VARCHAR(500)," +
                             "enabled BOOLEAN NOT NULL," +
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
                             "last_seen datetime," +
                             "cover_art_scheme VARCHAR(191) NOT NULL," +
                             "transcode_scheme VARCHAR(191) NOT NULL," +
                             "dynamic_ip BOOLEAN DEFAULT 1 NOT NULL," +
                             "client_side_playlist BOOLEAN DEFAULT 0 NOT NULL," +
                             "jukebox BOOLEAN DEFAULT 0 NOT NULL," +
                             "technology VARCHAR(32) DEFAULT 'WEB' NOT NULL," +
                             "client_id VARCHAR(191)," +
                             "PRIMARY KEY (id))");
            LOG.info("Database table 'player' was created successfully.");
        }

        if (template.queryForInt("SELECT count(*) FROM role WHERE id = 6") == 0) {
            LOG.info("Role 'comment' not found in database. Creating it.");
            template.execute("INSERT INTO role VALUES (6, 'comment')");
            template.execute("INSERT INTO user_role " +
                             "SELECT distinct u.username, 6 FROM user u, user_role ur " +
                             "WHERE u.username = ur.username AND ur.role_id IN (1, 5)");
            LOG.info("Role 'comment' was created successfully.");
        }

        if (!columnExists(template, "bytes_streamed", "user")) {
            LOG.info("Database columns 'user.bytes_streamed/downloaded/uploaded' not found.  Creating them.");
            template.execute("ALTER TABLE user add bytes_streamed bigint default 0 NOT NULL");
            template.execute("ALTER TABLE user add bytes_downloaded bigint default 0 NOT NULL");
            template.execute("ALTER TABLE user add bytes_uploaded bigint default 0 NOT NULL");
            LOG.info("Database columns 'user.bytes_streamed/downloaded/uploaded' were added successfully.");
        }

        if (!tableExists(template, "user_settings")) {
            LOG.info("Database table 'user_settings' not found.  Creating it.");
            template.execute("CREATE TABLE user_settings (" +
                             "username VARCHAR(64) NOT NULL," +
                             "locale VARCHAR(32)," +
                             "theme_id VARCHAR(64)," +
                             "final_version_notification BOOLEAN DEFAULT 1 NOT NULL," +
                             "beta_version_notification BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_caption_cutoff int default 35 NOT NULL," +
                             "main_track_number BOOLEAN DEFAULT 1 NOT NULL," +
                             "main_artist BOOLEAN DEFAULT 1 NOT NULL," +
                             "main_album BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_genre BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_year BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_bit_rate BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_duration BOOLEAN DEFAULT 1 NOT NULL," +
                             "main_format BOOLEAN DEFAULT 0 NOT NULL," +
                             "main_file_size BOOLEAN DEFAULT 0 NOT NULL," +
                             "playlist_caption_cutoff int default 35 NOT NULL," +
                             "playlist_track_number BOOLEAN DEFAULT 0 NOT NULL," +
                             "playlist_artist BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_album BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_genre BOOLEAN DEFAULT 0 NOT NULL," +
                             "playlist_year BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_bit_rate BOOLEAN DEFAULT 0 NOT NULL," +
                             "playlist_duration BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_format BOOLEAN DEFAULT 1 NOT NULL," +
                             "playlist_file_size BOOLEAN DEFAULT 1 NOT NULL," +
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
            LOG.info("Database table 'player_transcoding' was created successfully.");
        }

        if (!tableExists(template, "user_rating")) {
            LOG.info("Database table 'user_rating' not found.  Creating it.");
            template.execute("CREATE TABLE user_rating (" +
                             "username VARCHAR(64) NOT NULL," +
                             "path VARCHAR(500) NOT NULL," +
                             "rating double NOT NULL," +
                             "PRIMARY KEY (username, path)," +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE) ROW_FORMAT = DYNAMIC");
            LOG.info("Database table 'user_rating' was created successfully.");

            template.execute("INSERT INTO user_rating SELECT 'admin', path, rating FROM music_file_info " +
                             "WHERE rating is NOT NULL and rating > 0");
            LOG.info("Migrated data FROM 'music_file_info' to 'user_rating'.");
        }

        if (!columnExists(template, "last_fm_enabled", "user_settings")) {
            LOG.info("Database columns 'user_settings.last_fm_*' not found.  Creating them.");
            template.execute("ALTER TABLE user_settings add last_fm_enabled BOOLEAN DEFAULT 0 NOT NULL");
            template.execute("ALTER TABLE user_settings add last_fm_username VARCHAR(64) null");
            template.execute("ALTER TABLE user_settings add last_fm_password VARCHAR(191) null");
            LOG.info("Database columns 'user_settings.last_fm_*' were added successfully.");
        }

        if (!columnExists(template, "transcode_scheme", "user_settings")) {
            LOG.info("Database column 'user_settings.transcode_scheme' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add transcode_scheme VARCHAR(191) default '" +
                             TranscodeScheme.OFF.name() + "' NOT NULL");
            LOG.info("Database column 'user_settings.transcode_scheme' was added successfully.");
        }

        if (!columnExists(template, "enabled", "music_file_info")) {
            LOG.info("Database column 'music_file_info.enabled' not found.  Creating it.");
            template.execute("ALTER TABLE music_file_info add enabled BOOLEAN DEFAULT 1 NOT NULL");
            LOG.info("Database column 'music_file_info.enabled' was added successfully.");
        }

        if (!columnExists(template, "show_now_playing", "user_settings")) {
            LOG.info("Database column 'user_settings.show_now_playing' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add show_now_playing BOOLEAN DEFAULT 1 NOT NULL");
            LOG.info("Database column 'user_settings.show_now_playing' was added successfully.");
        }

        if (!columnExists(template, "selected_music_folder_id", "user_settings")) {
            LOG.info("Database column 'user_settings.selected_music_folder_id' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add selected_music_folder_id int default -1 NOT NULL");
            LOG.info("Database column 'user_settings.selected_music_folder_id' was added successfully.");
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
                             "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");
            LOG.info("Database table 'podcast_channel' was created successfully.");
        }

        if (!tableExists(template, "podcast_episode")) {
            LOG.info("Database table 'podcast_episode' not found.  Creating it.");
            template.execute("CREATE TABLE podcast_episode (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "channel_id int NOT NULL," +
                             "url VARCHAR(500) NOT NULL," +
                             "path VARCHAR(500)," +
                             "title VARCHAR(191)," +
                             "description VARCHAR(500)," +
                             "publish_date datetime," +
                             "duration VARCHAR(191)," +
                             "bytes_total bigint," +
                             "bytes_downloaded bigint," +
                             "status VARCHAR(191) NOT NULL," +
                             "error_message VARCHAR(500)," +
                             "PRIMARY KEY (id)," +
                             "FOREIGN KEY (channel_id) REFERENCES podcast_channel(id) ON DELETE CASCADE) ROW_FORMAT = DYNAMIC");
            LOG.info("Database table 'podcast_episode' was created successfully.");
        }

        if (template.queryForInt("SELECT count(*) FROM role WHERE id = 7") == 0) {
            LOG.info("Role 'podcast' not found in database. Creating it.");
            template.execute("INSERT INTO role VALUES (7, 'podcast')");
            template.execute("INSERT INTO user_role " +
                             "SELECT distinct u.username, 7 FROM user u, user_role ur " +
                             "WHERE u.username = ur.username and ur.role_id = 1");
            LOG.info("Role 'podcast' was created successfully.");
        }

        if (!columnExists(template, "ldap_authenticated", "user")) {
            LOG.info("Database column 'user.ldap_authenticated' not found.  Creating it.");
            template.execute("ALTER TABLE user add ldap_authenticated BOOLEAN DEFAULT 0 NOT NULL");
            LOG.info("Database column 'user.ldap_authenticated' was added successfully.");
        }

        if (!columnExists(template, "party_mode_enabled", "user_settings")) {
            LOG.info("Database column 'user_settings.party_mode_enabled' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add party_mode_enabled BOOLEAN DEFAULT 0 NOT NULL");
            LOG.info("Database column 'user_settings.party_mode_enabled' was added successfully.");
        }

        if (!columnExists(template, "now_playing_allowed", "user_settings")) {
            LOG.info("Database column 'user_settings.now_playing_allowed' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add now_playing_allowed BOOLEAN DEFAULT 1 NOT NULL");
            LOG.info("Database column 'user_settings.now_playing_allowed' was added successfully.");
        }

        if (!columnExists(template, "web_player_default", "user_settings")) {
            LOG.info("Database column 'user_settings.web_player_default' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add web_player_default BOOLEAN DEFAULT 0 NOT NULL");
            LOG.info("Database column 'user_settings.web_player_default' was added successfully.");
        }

        if (template.queryForInt("SELECT count(*) FROM role WHERE id = 8") == 0) {
            LOG.info("Role 'stream' not found in database. Creating it.");
            template.execute("INSERT INTO role VALUES (8, 'stream')");
            template.execute("INSERT INTO user_role SELECT distinct u.username, 8 FROM user u");
            LOG.info("Role 'stream' was created successfully.");
        }

        if (!tableExists(template, "system_avatar")) {
            LOG.info("Database table 'system_avatar' not found.  Creating it.");
            template.execute("CREATE TABLE system_avatar (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "name VARCHAR(64)," +
                             "created_date datetime NOT NULL," +
                             "mime_type VARCHAR(191) NOT NULL," +
                             "width int NOT NULL," +
                             "height int NOT NULL," +
                             "data BLOB NOT NULL," +
                             "PRIMARY KEY (id))");
            LOG.info("Database table 'system_avatar' was created successfully.");
        }

        for (String avatar : AVATARS) {
            createAvatar(template, avatar);
        }

        if (!tableExists(template, "custom_avatar")) {
            LOG.info("Database table 'custom_avatar' not found.  Creating it.");
            template.execute("CREATE TABLE custom_avatar (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "name VARCHAR(64)," +
                             "created_date datetime NOT NULL," +
                             "mime_type VARCHAR(191) NOT NULL," +
                             "width int NOT NULL," +
                             "height int NOT NULL," +
                             "data BLOB NOT NULL," +
                             "username VARCHAR(64) NOT NULL," +
                             "PRIMARY KEY (id)," +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE)");
            LOG.info("Database table 'custom_avatar' was created successfully.");
        }

        if (!columnExists(template, "avatar_scheme", "user_settings")) {
            LOG.info("Database column 'user_settings.avatar_scheme' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add avatar_scheme VARCHAR(191) default 'NONE' NOT NULL");
            LOG.info("Database column 'user_settings.avatar_scheme' was added successfully.");
        }

        if (!columnExists(template, "system_avatar_id", "user_settings")) {
            LOG.info("Database column 'user_settings.system_avatar_id' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add system_avatar_id int");
            template.execute("ALTER TABLE user_settings add FOREIGN KEY (system_avatar_id) REFERENCES system_avatar(id)");
            LOG.info("Database column 'user_settings.system_avatar_id' was added successfully.");
        }

        if (template.queryForInt("SELECT count(*) FROM role WHERE id = 9") == 0) {
            LOG.info("Role 'settings' not found in database. Creating it.");
            template.execute("INSERT INTO role VALUES (9, 'settings')");
            template.execute("INSERT INTO user_role SELECT distinct u.username, 9 FROM user u");
            LOG.info("Role 'settings' was created successfully.");
        }

        if (template.queryForInt("SELECT count(*) FROM role WHERE id = 10") == 0) {
            LOG.info("Role 'jukebox' not found in database. Creating it.");
            template.execute("INSERT INTO role VALUES (10, 'jukebox')");
            template.execute("INSERT INTO user_role " +
                             "SELECT distinct u.username, 10 FROM user u, user_role ur " +
                             "WHERE u.username = ur.username and ur.role_id = 1");
            LOG.info("Role 'jukebox' was created successfully.");
        }

        if (!columnExists(template, "changed", "music_folder")) {
            LOG.info("Database column 'music_folder.changed' not found.  Creating it.");
            template.execute("ALTER TABLE music_folder add changed datetime default 0 NOT NULL");
            LOG.info("Database column 'music_folder.changed' was added successfully.");
        }

        if (!columnExists(template, "changed", "internet_radio")) {
            LOG.info("Database column 'internet_radio.changed' not found.  Creating it.");
            template.execute("ALTER TABLE internet_radio add changed datetime default 0 NOT NULL");
            LOG.info("Database column 'internet_radio.changed' was added successfully.");
        }

        if (!columnExists(template, "changed", "user_settings")) {
            LOG.info("Database column 'user_settings.changed' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add changed datetime default 0 NOT NULL");
            LOG.info("Database column 'user_settings.changed' was added successfully.");
        }

        if (!columnExists(template, "show_chat", "user_settings")) {
            LOG.info("Database column 'user_settings.show_chat' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add show_chat BOOLEAN DEFAULT 1 NOT NULL");
            LOG.info("Database column 'user_settings.show_chat' was added successfully.");
        }

        for (String format : Arrays.asList("avi", "mpg", "mpeg", "mp4", "m4v", "mkv", "mov", "wmv", "ogv")) {
            template.update("delete FROM transcoding WHERE source_format=? and target_format=?", new Object[] {format, "flv"});
            template.execute("INSERT INTO transcoding VALUES(null,'" + format + " > flv' ,'" + format + "' ,'flv','ffmpeg -ss %o -i %s -async 1 -b %bk -s %wx%h -ar 44100 -ac 2 -v 0 -f flv -',null,null,1,1)");
            template.execute("INSERT INTO player_transcoding SELECT p.id as player_id, t.id as transaction_id FROM player p, transcoding t WHERE t.name = '" + format + " > flv'");
        }
        LOG.info("Created video transcoding configuration.");

        if (!columnExists(template, "email", "user")) {
            LOG.info("Database column 'user.email' not found.  Creating it.");
            template.execute("ALTER TABLE user add email VARCHAR(191)");
            LOG.info("Database column 'user.email' was added successfully.");
        }

        if (template.queryForInt("SELECT count(*) FROM role WHERE id = 11") == 0) {
            LOG.info("Role 'share' not found in database. Creating it.");
            template.execute("INSERT INTO role VALUES (11, 'share')");
            template.execute("INSERT INTO user_role " +
                             "SELECT distinct u.username, 11 FROM user u, user_role ur " +
                             "WHERE u.username = ur.username and ur.role_id = 1");
            LOG.info("Role 'share' was created successfully.");
        }

        if (!tableExists(template, "share")) {
            LOG.info("Table 'share' not found in database. Creating it.");
            template.execute("CREATE TABLE share (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "name VARCHAR(64) NOT NULL," +
                    "description VARCHAR(191)," +
                    "username VARCHAR(64) NOT NULL," +
                    "created datetime NOT NULL," +
                    "expires datetime," +
                    "last_visited datetime," +
                    "visit_count int default 0 NOT NULL," +
                    "unique (name)," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE)");
            template.execute("create index idx_share_name on share(name)");

            LOG.info("Table 'share' was created successfully.");
            LOG.info("Table 'share_file' not found in database. Creating it.");
            template.execute("CREATE TABLE share_file (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "share_id int NOT NULL," +
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
                             "player_id int NOT NULL," +
                             "transcoding_id int NOT NULL," +
                             "PRIMARY KEY (player_id, transcoding_id)," +
                             "FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE," +
                             "FOREIGN KEY (transcoding_id) REFERENCES transcoding2(id) ON DELETE CASCADE)");

            template.execute("INSERT INTO player_transcoding2(player_id, transcoding_id) " +
                    "SELECT distinct p.id, t.id FROM player p, transcoding2 t");

            template.execute("INSERT INTO player_transcoding2(player_id, transcoding_id) " +
                    "SELECT distinct p.id, t.id FROM player p, transcoding2 t WHERE t.name='mkv video'");

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
                    "title VARCHAR(191)," +
                    "album VARCHAR(191)," +
                    "artist VARCHAR(191)," +
                    "album_artist VARCHAR(191)," +
                    "disc_number int," +
                    "track_number int," +
                    "year int," +
                    "genre VARCHAR(191)," +
                    "bit_rate int," +
                    "variable_bit_rate BOOLEAN NOT NULL," +
                    "duration_seconds int," +
                    "file_size bigint," +
                    "width int," +
                    "height int," +
                    "cover_art_path VARCHAR(500)," +
                    "parent_path VARCHAR(500)," +
                    "play_count int NOT NULL," +
                    "last_played datetime," +
                    "comment VARCHAR(500)," +
                    "created datetime NOT NULL," +
                    "changed datetime NOT NULL," +
                    "last_scanned datetime NOT NULL," +
                    "children_last_updated datetime NOT NULL," +
                    "present BOOLEAN NOT NULL," +
                    "version int NOT NULL," +
                    "unique (path)," +
                    "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");

            template.execute("create index idx_media_file_path on media_file(path)");
            template.execute("create index idx_media_file_parent_path on media_file(parent_path)");
            template.execute("create index idx_media_file_type on media_file(type)");
            template.execute("create index idx_media_file_album on media_file(album)");
            template.execute("create index idx_media_file_artist on media_file(artist)");
            template.execute("create index idx_media_file_album_artist on media_file(album_artist)");
            template.execute("create index idx_media_file_present on media_file(present)");
            template.execute("create index idx_media_file_genre on media_file(genre)");
            template.execute("create index idx_media_file_play_count on media_file(play_count)");
            template.execute("create index idx_media_file_created on media_file(created)");
            template.execute("create index idx_media_file_last_played on media_file(last_played)");

            LOG.info("Database table 'media_file' was created successfully.");
        }

        if (!tableExists(template, "artist")) {
            LOG.info("Database table 'artist' not found.  Creating it.");
            template.execute("CREATE TABLE artist (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "name VARCHAR(191) NOT NULL," +
                    "cover_art_path VARCHAR(500)," +
                    "album_count int default 0 NOT NULL," +
                    "last_scanned datetime NOT NULL," +
                    "present BOOLEAN NOT NULL," +
                    "unique (name)," +
                    "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");

            template.execute("create index idx_artist_name on artist(name)");
            template.execute("create index idx_artist_present on artist(present)");

            LOG.info("Database table 'artist' was created successfully.");
        }

        if (!tableExists(template, "album")) {
            LOG.info("Database table 'album' not found.  Creating it.");
            template.execute("CREATE TABLE album (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "path VARCHAR(500) NOT NULL," +
                    "name VARCHAR(191) NOT NULL," +
                    "artist VARCHAR(191) NOT NULL," +
                    "song_count int default 0 NOT NULL," +
                    "duration_seconds int default 0 NOT NULL," +
                    "cover_art_path VARCHAR(500)," +
                    "play_count int default 0 NOT NULL," +
                    "last_played datetime," +
                    "comment VARCHAR(500)," +
                    "created datetime NOT NULL," +
                    "last_scanned datetime NOT NULL," +
                    "present BOOLEAN NOT NULL," +
                    "unique (artist, name)," +
                    "PRIMARY KEY (id)) ROW_FORMAT = DYNAMIC");

            template.execute("create index idx_album_artist_name on album(artist, name)");
            template.execute("create index idx_album_play_count on album(play_count)");
            template.execute("create index idx_album_last_played on album(last_played)");
            template.execute("create index idx_album_present on album(present)");

            LOG.info("Database table 'album' was created successfully.");
        }

        // Added in 4.7.beta3
        if (!rowExists(template, "table_name='ALBUM' and column_name='NAME' and ordinal_position=1",
                "information_schema.system_indexinfo")) {
            template.execute("create index idx_album_name on album(name)");
        }

        if (!tableExists(template, "starred_media_file")) {
            LOG.info("Database table 'starred_media_file' not found.  Creating it.");
            template.execute("CREATE TABLE starred_media_file (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "media_file_id int NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "created datetime NOT NULL," +
                    "FOREIGN KEY (media_file_id) REFERENCES media_file(id) ON DELETE CASCADE,"+
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE," +
                    "unique (media_file_id, username)," +
                    "PRIMARY KEY (id))");

            template.execute("create index idx_starred_media_file_media_file_id on starred_media_file(media_file_id)");
            template.execute("create index idx_starred_media_file_username on starred_media_file(username)");

            LOG.info("Database table 'starred_media_file' was created successfully.");
        }

        if (!tableExists(template, "starred_album")) {
            LOG.info("Database table 'starred_album' not found.  Creating it.");
            template.execute("CREATE TABLE starred_album (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "album_id int NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "created datetime NOT NULL," +
                    "FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE," +
                    "unique (album_id, username)," +
                    "PRIMARY KEY (id))");

            template.execute("create index idx_starred_album_album_id on starred_album(album_id)");
            template.execute("create index idx_starred_album_username on starred_album(username)");

            LOG.info("Database table 'starred_album' was created successfully.");
        }

        if (!tableExists(template, "starred_artist")) {
            LOG.info("Database table 'starred_artist' not found.  Creating it.");
            template.execute("CREATE TABLE starred_artist (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "artist_id int NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "created datetime NOT NULL," +
                    "FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE,"+
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE," +
                    "unique (artist_id, username)," +
                    "PRIMARY KEY (id))");

            template.execute("create index idx_starred_artist_artist_id on starred_artist(artist_id)");
            template.execute("create index idx_starred_artist_username on starred_artist(username)");

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
                    "file_count int default 0 NOT NULL," +
                    "duration_seconds int default 0 NOT NULL," +
                    "created datetime NOT NULL," +
                    "changed datetime NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE) ROW_FORMAT = DYNAMIC");

            LOG.info("Database table 'playlist' was created successfully.");
        }

        if (!columnExists(template, "imported_FROM", "playlist")) {
            LOG.info("Database column 'playlist.imported_FROM' not found.  Creating it.");
            template.execute("ALTER TABLE playlist add imported_FROM VARCHAR(191)");
            LOG.info("Database column 'playlist.imported_FROM' was added successfully.");
        }

        if (!tableExists(template, "playlist_file")) {
            LOG.info("Database table 'playlist_file' not found.  Creating it.");
            template.execute("CREATE TABLE playlist_file (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "playlist_id int NOT NULL," +
                    "media_file_id int NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (media_file_id) REFERENCES media_file(id) ON DELETE CASCADE)");

            LOG.info("Database table 'playlist_file' was created successfully.");
        }

        if (!tableExists(template, "playlist_user")) {
            LOG.info("Database table 'playlist_user' not found.  Creating it.");
            template.execute("CREATE TABLE playlist_user (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "playlist_id int NOT NULL," +
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
                    "media_file_id int NOT NULL," +
                    "position_millis bigint NOT NULL," +
                    "username VARCHAR(64) NOT NULL," +
                    "comment VARCHAR(500)," +
                    "created datetime NOT NULL," +
                    "changed datetime NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (media_file_id) REFERENCES media_file(id) ON DELETE CASCADE,"+
                    "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE," +
                    "unique (media_file_id, username)) ROW_FORMAT = DYNAMIC");

            template.execute("create index idx_bookmark_media_file_id on bookmark(media_file_id)");
            template.execute("create index idx_bookmark_username on bookmark(username)");

            LOG.info("Database table 'bookmark' was created successfully.");
        }

        if (!columnExists(template, "year", "album")) {
            LOG.info("Database column 'album.year' not found.  Creating it.");
            template.execute("ALTER TABLE album add year int");
            LOG.info("Database column 'album.year' was added successfully.");
        }

        if (!columnExists(template, "genre", "album")) {
            LOG.info("Database column 'album.genre' not found.  Creating it.");
            template.execute("ALTER TABLE album add genre VARCHAR(191)");
            LOG.info("Database column 'album.genre' was added successfully.");
        }

        if (!tableExists(template, "genre")) {
            LOG.info("Database table 'genre' not found.  Creating it.");
            template.execute("CREATE TABLE genre (" +
                    "name VARCHAR(191) NOT NULL," +
                    "song_count int NOT NULL)");

            LOG.info("Database table 'genre' was created successfully.");
        }

        if (!columnExists(template, "album_count", "genre")) {
            LOG.info("Database column 'genre.album_count' not found.  Creating it.");
            template.execute("ALTER TABLE genre add album_count int default 0 NOT NULL");
            LOG.info("Database column 'genre.album_count' was added successfully.");
        }



        if (!columnExists(template, "song_notification", "user_settings")) {
            LOG.info("Database column 'user_settings.song_notification' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add song_notification BOOLEAN default 1 NOT NULL");
            LOG.info("Database column 'user_settings.song_notification' was added successfully.");
        }



        if (!columnExists(template, "show_artist_info", "user_settings")) {
            LOG.info("Database column 'user_settings.show_artist_info' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add show_artist_info BOOLEAN default 1 NOT NULL");
            LOG.info("Database column 'user_settings.show_artist_info' was added successfully.");
        }

        if (!columnExists(template, "auto_hide_play_queue", "user_settings")) {
            LOG.info("Database column 'user_settings.auto_hide_play_queue' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add auto_hide_play_queue BOOLEAN default 1 NOT NULL");
            LOG.info("Database column 'user_settings.auto_hide_play_queue' was added successfully.");
        }

        if (!columnExists(template, "view_as_list", "user_settings")) {
            LOG.info("Database column 'user_settings.view_as_list' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add view_as_list BOOLEAN default 0 NOT NULL");
            LOG.info("Database column 'user_settings.view_as_list' was added successfully.");
        }

        if (!tableExists(template, "music_folder_user")) {
            LOG.info("Database table 'music_folder_user' not found.  Creating it.");
            template.execute("CREATE TABLE music_folder_user (" +
                             "music_folder_id INT NOT NULL," +
                             "username VARCHAR(64) NOT NULL, " +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE, " +
                             "FOREIGN KEY (music_folder_id) REFERENCES music_folder(id) ON DELETE CASCADE)");
            template.execute("create index idx_music_folder_user_username on music_folder_user(username)");
            template.execute("INSERT INTO music_folder_user SELECT music_folder.id, user.username FROM music_folder, user");
            LOG.info("Database table 'music_folder_user' was created successfully.");
        }

        if (!columnExists(template, "folder_id", "album")) {
            LOG.info("Database column 'album.folder_id' not found.  Creating it.");
            template.execute("ALTER TABLE album add folder_id int");
            LOG.info("Database column 'album.folder_id' was added successfully.");
        }

        if (!tableExists(template, "play_queue")) {
            LOG.info("Database table 'play_queue' not found.  Creating it.");
            template.execute("CREATE TABLE play_queue (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "username VARCHAR(64) NOT NULL," +
                             "current int," +
                             "position_millis bigint," +
                             "changed datetime NOT NULL," +
                             "changed_by VARCHAR(191) NOT NULL," +
                             "PRIMARY KEY (id)," +
                             "FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE)");
            LOG.info("Database table 'play_queue' was created successfully.");
        }

        if (!tableExists(template, "play_queue_file")) {
            LOG.info("Database table 'play_queue_file' not found.  Creating it.");
            template.execute("CREATE TABLE play_queue_file (" +
                             "id INT NOT NULL AUTO_INCREMENT," +
                             "play_queue_id int NOT NULL," +
                             "media_file_id int NOT NULL," +
                             "PRIMARY KEY (id)," +
                             "FOREIGN KEY (play_queue_id) REFERENCES play_queue(id) ON DELETE CASCADE," +
                             "FOREIGN KEY (media_file_id) REFERENCES media_file(id) ON DELETE CASCADE)");

            LOG.info("Database table 'play_queue_file' was created successfully.");
        }

        if (!rowExists(template, "table_name='PODCAST_EPISODE' and column_name='URL' and ordinal_position=1",
                       "information_schema.system_indexinfo")) {
            template.execute("create index idx_podcast_episode_url on podcast_episode(url)");
            LOG.info("Created index for podcast_episode.url");
        }

        if (!columnExists(template, "default_album_list", "user_settings")) {
            LOG.info("Database column 'user_settings.default_album_list' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add default_album_list VARCHAR(191) default '" +
                             AlbumListType.RANDOM.getId() + "' NOT NULL");
            LOG.info("Database column 'user_settings.default_album_list' was added successfully.");
        }

        if (!columnExists(template, "queue_following_songs", "user_settings")) {
            LOG.info("Database column 'user_settings.queue_following_songs' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add queue_following_songs BOOLEAN DEFAULT 1 NOT NULL");
            LOG.info("Database column 'user_settings.queue_following_songs' was added successfully.");
        }

        if (!columnExists(template, "image_url", "podcast_channel")) {
            LOG.info("Database column 'podcast_channel.image_url' not found.  Creating it.");
            template.execute("ALTER TABLE podcast_channel add image_url VARCHAR(191)");
            LOG.info("Database column 'podcast_channel.image_url' was added successfully.");
        }

        if (!columnExists(template, "show_side_bar", "user_settings")) {
            LOG.info("Database column 'user_settings.show_side_bar' not found.  Creating it.");
            template.execute("ALTER TABLE user_settings add show_side_bar BOOLEAN DEFAULT 1 NOT NULL");
            LOG.info("Database column 'user_settings.show_side_bar' was added successfully.");
        }

        if (!columnExists(template, "folder_id", "artist")) {
            LOG.info("Database column 'artist.folder_id' not found.  Creating it.");
            template.execute("ALTER TABLE artist add folder_id int");
            LOG.info("Database column 'artist.folder_id' was added successfully.");
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