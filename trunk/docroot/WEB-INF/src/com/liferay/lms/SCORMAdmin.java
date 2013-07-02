package com.liferay.lms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.io.FileUtils;

import com.liferay.lms.model.SCORMContent;
import com.liferay.lms.service.SCORMContentLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class SCORMAdmin
 */
public class SCORMAdmin extends MVCPortlet 
{
	
	public void saveSCORM(ActionRequest actRequest, ActionResponse response) throws PortalException, SystemException, IOException
	{
		ThemeDisplay themeDisplay = (ThemeDisplay) actRequest.getAttribute(WebKeys.THEME_DISPLAY);
		UploadPortletRequest request = PortalUtil.getUploadPortletRequest(actRequest);
		String title=ParamUtil.getString(request, "title");
		String description=ParamUtil.getString(request, "description","");
		String redirect=ParamUtil.getString(request, "redirect","");
		long scormId=ParamUtil.getLong(request, "scormId",0);
		ServiceContext serviceContext =  ServiceContextFactory.getInstance(SCORMContent.class.getName(), request);

		if(scormId==0)	
		{
			
			String fileName = request.getFileName("fileName");
			File file = request.getFile("fileName");
		
			if(fileName!=null && !fileName.equals(""))
			{	
				if (!file.getName().toLowerCase().endsWith(".zip")) {
					SessionErrors.add(actRequest, "scormadmin.error.nozip");
				} else {
					try {
						ZipFile zipFile= new ZipFile(file);
						if (zipFile.getEntry("imsmanifest.xml") == null) {
							SessionErrors.add(actRequest, "scormadmin.error.nomanifest");
						}
					} catch (ZipException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (!SessionErrors.isEmpty(actRequest)) {
					response.setRenderParameters(actRequest.getParameterMap());
					response.setRenderParameter("jspPage", "/html/scormadmin/editscorm.jsp");
				} else {
					try 
					{
						SCORMContentLocalServiceUtil.addSCORMContent(title, description, file, serviceContext);
					} 
					catch (IOException e) 
					{
							// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else
		{
			SCORMContent scorm=SCORMContentLocalServiceUtil.getSCORMContent(scormId);
			scorm.setTitle(title);
			scorm.setDescription(description);
			SCORMContentLocalServiceUtil.updateSCORMContent(scorm, serviceContext);
		}
		if(redirect!=null &&!"".equals(redirect))
		{
			response.sendRedirect(redirect);
		}
		
	}
	public void deleteSCORM(ActionRequest actRequest, ActionResponse response) throws PortalException, SystemException, IOException
	{
		ThemeDisplay themeDisplay = (ThemeDisplay) actRequest.getAttribute(WebKeys.THEME_DISPLAY);
		UploadPortletRequest request = PortalUtil.getUploadPortletRequest(actRequest);
		long scormId=ParamUtil.getLong(request, "scormId",0);
		if(scormId>0)
		{
			SCORMContentLocalServiceUtil.delete(scormId);
		}
		String redirect=ParamUtil.getString(actRequest, "redirect","");
		if(redirect!=null &&!"".equals(redirect))
		{
			response.sendRedirect(redirect);
		}
		
	}

}
