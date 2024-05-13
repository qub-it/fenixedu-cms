/**
 * Copyright © 2014 Instituto Superior Técnico
 *
 * This file is part of FenixEdu CMS.
 *
 * FenixEdu CMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu CMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu CMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.cms.ui;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.fenixedu.cms.domain.CMSTheme;
import org.fenixedu.cms.domain.CMSThemeFile;
import org.joda.time.DateTime;

@Path("/cms/assets")
public class CMSAssetsController {

    private static final String expires = DateTime.now().plusYears(1).toString("E, d MMM yyyy HH:mm:ss z");

    @Path("/{type}/{hash}/{path:.*}")
    @GET
    public void asset(@PathParam("type") String type, @PathParam("hash") String hash, @PathParam("path") String path,
            @Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
        CMSTheme theme = CMSTheme.forType(type);
        if (theme == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, type);
            return;
        }

        CMSThemeFile file = theme.fileForPath("static/" + path);
        if (file == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
            return;
        }
        byte[] bytes = file.getContent();
        String etag = "W/\"" + bytes.length + "-" + file.getLastModified().getMillis() + "\"";

        response.setHeader("ETag", etag);
        response.setHeader("Expires", expires);
        response.setHeader("Cache-Control", "max-age=31536000");

        if (etag.equals(request.getHeader("If-None-Match"))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setContentLength(bytes.length);
            response.setContentType(file.getContentType());
            try (OutputStream stream = response.getOutputStream()) {
                stream.write(bytes);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
