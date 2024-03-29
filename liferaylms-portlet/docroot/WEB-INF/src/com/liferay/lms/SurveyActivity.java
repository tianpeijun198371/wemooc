package com.liferay.lms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.beanutils.BeanComparator;
import org.jsoup.Jsoup;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.liferay.counter.service.CounterLocalServiceUtil;
import com.liferay.lms.learningactivity.questiontype.QuestionType;
import com.liferay.lms.learningactivity.questiontype.QuestionTypeRegistry;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.LearningActivityTry;
import com.liferay.lms.model.SurveyResult;
import com.liferay.lms.model.TestAnswer;
import com.liferay.lms.model.TestQuestion;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.LearningActivityTryLocalServiceUtil;
import com.liferay.lms.service.SurveyResultLocalServiceUtil;
import com.liferay.lms.service.TestAnswerLocalServiceUtil;
import com.liferay.lms.service.TestQuestionLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.portlet.asset.model.AssetRendererFactory;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.tls.lms.util.LiferaylmsUtil;


/**
 * Portlet implementation class SurveyActivity
 */
public class SurveyActivity extends MVCPortlet {
	
	HashMap<Long, TestAnswer> answersMap = new HashMap<Long, TestAnswer>(); 
 
	public void saveSurvey(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		
		//com.liferay.lms.model.SurveyActivity surveyActivity = null;
		
		//SurveyActivityLocalServiceUtil.addSurveyActivity(surveyActivity);
	}
		
	public void correct(ActionRequest actionRequest,ActionResponse actionResponse)throws Exception {		

		int score = 100;
		long latId=ParamUtil.getLong(actionRequest,"latId" );
		long actId=ParamUtil.getLong(actionRequest,"actId",0 );
		
		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		
		Enumeration<String> params=actionRequest.getParameterNames();
		java.util.Hashtable<TestQuestion, TestAnswer> resultados=new java.util.Hashtable<TestQuestion, TestAnswer>();
		java.util.Hashtable<TestQuestion, String> respuestaLibre = new java.util.Hashtable<TestQuestion, String>();
		
		LearningActivityTry larntry=LearningActivityTryLocalServiceUtil.getLearningActivityTry(latId);

		//Comprobar si el usuario se dejo alguna encuesta abierta
		if (larntry.getEndDate() == null )
		{
		    while(params.hasMoreElements())
			{
				String param=params.nextElement();
				if(param.startsWith("question_"))
				{
					String squestionId=param.substring("question_".length());
					long questionId=Long.parseLong(squestionId);
					new TestQuestionLocalServiceUtil();
					TestQuestion question=TestQuestionLocalServiceUtil.getTestQuestion(questionId);
					
					// Preparamos para guardar respuesta del usuario
					SurveyResult surveyResult = SurveyResultLocalServiceUtil.createSurveyResult(CounterLocalServiceUtil.increment(SurveyResult.class.getName()));
					TestAnswer testAnswer = null;
					String respuesta = actionRequest.getParameter(param);
					List<TestAnswer> testAnswerList = TestAnswerLocalServiceUtil.getTestAnswersByQuestionId(questionId); 
					if(testAnswerList != null && testAnswerList.size() > 0 ){
						for( TestAnswer t : testAnswerList){
							if( respuesta.equalsIgnoreCase(String.valueOf(t.getAnswerId()))){
								String textoRespuesta = t.getAnswer();
								respuesta = textoRespuesta.substring(textoRespuesta.indexOf(">") + 1, textoRespuesta.indexOf("</"));
								resultados.put(question, t);						
								//Guardar la encuesta para las estadisticas.
								surveyResult.setActId(actId);
								surveyResult.setLatId(latId);
								surveyResult.setQuestionId(questionId);
								surveyResult.setAnswerId(Long.valueOf(t.getAnswerId()));
								surveyResult.setUserId(themeDisplay.getUserId());
								surveyResult.setFreeAnswer(respuesta);
								SurveyResultLocalServiceUtil.updateSurveyResult(surveyResult, true);
								break;
								}
							}
						} else {
						surveyResult.setActId(actId);
						surveyResult.setLatId(latId);
						surveyResult.setQuestionId(questionId);
						surveyResult.setAnswerId(0);// PARA TEXTO LIBRE EL answerId = 0
						surveyResult.setUserId(themeDisplay.getUserId());
						surveyResult.setFreeAnswer(respuesta);
						SurveyResultLocalServiceUtil.updateSurveyResult(surveyResult, true);								
						respuestaLibre.put(question, respuesta);
					}
				}
			}
			
			//Crear xml para guardar las respuestas
			Element resultadosXML=SAXReaderUtil.createElement("results");
			Document resultadosXMLDoc=SAXReaderUtil.createDocument(resultadosXML);
			for(TestQuestion question:resultados.keySet())
			{
				TestAnswer answer=resultados.get(question);
				if(answer != null){
					Element questionXML=SAXReaderUtil.createElement("question");
					questionXML.addAttribute("id", Long.toString(question.getQuestionId()));
					Element answerXML=SAXReaderUtil.createElement("answer");
					answerXML.addAttribute("id", Long.toString(answer.getAnswerId()));
					questionXML.add(answerXML);
					resultadosXML.add(questionXML);
				}
			}

			for(TestQuestion question:respuestaLibre.keySet())
			{
				String respuestaFree = respuestaLibre.get(question);
				Element questionXML=SAXReaderUtil.createElement("question");
				questionXML.addAttribute("id", Long.toString(question.getQuestionId()));
				Element answerXML=SAXReaderUtil.createElement("answer");
				answerXML.addAttribute("respuestaFree", respuestaFree);
				questionXML.add(answerXML);
				resultadosXML.add(questionXML);					
			}
			//Guardar los resultados
			//LearningActivityTry larntry=LearningActivityTryLocalServiceUtil.getLearningActivityTry(latId);
			
			larntry.setResult(score);
			larntry.setTryResultData(resultadosXMLDoc.formattedString());
			larntry.setEndDate(new java.util.Date(System.currentTimeMillis()));
			LearningActivityTryLocalServiceUtil.updateLearningActivityTry(larntry);
		}
		actionResponse.setRenderParameters(actionRequest.getParameterMap());
		actionRequest.setAttribute("resultados", resultados);
		
		actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/view.jsp");
		
	}
	
	public void edit(ActionRequest actionRequest,ActionResponse actionResponse)throws Exception {

		actionResponse.setRenderParameters(actionRequest.getParameterMap());
		if(ParamUtil.getLong(actionRequest, "actId", 0)==0)
		{
			actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/view.jsp");
		}
	}
	
	public void addquestion(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {
		
		long actid = ParamUtil.getLong(actionRequest, "resId");
	
		String text = ParamUtil.getString(actionRequest, "text");
		long questionType = ParamUtil.getLong(actionRequest, "qtype");
		
		TestQuestion question = TestQuestionLocalServiceUtil.addQuestion(actid, text, questionType);
		LearningActivity learnact = LearningActivityLocalServiceUtil.getLearningActivity(actid);
		
		actionResponse.setRenderParameter("questionId", Long.toString(question.getQuestionId()));
		actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
		actionResponse.setRenderParameter("resId", Long.toString(actid));
		actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/editquestion.jsp");
		
		actionRequest.setAttribute("activity", learnact);
		actionRequest.setAttribute("questionId", question.getQuestionId());
		
		actionRequest.setAttribute("primKey", question.getQuestionId());
	}
	
	public void editquestion(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		
		String text = ParamUtil.getString(actionRequest, "text","");
		long questionType = ParamUtil.getLong(actionRequest, "qtype",0);
		long questionId = ParamUtil.getLong(actionRequest, "questionId",0);
	
		TestQuestion question = TestQuestionLocalServiceUtil.getTestQuestion(questionId);
		
		question.setQuestionType(questionType);
		question.setText(text);
		
		TestQuestionLocalServiceUtil.updateTestQuestion(question);
		SessionMessages.add(actionRequest, "question-modified-successfully");
		
		actionResponse.setRenderParameter("questionId", Long.toString(questionId));
		actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
		actionResponse.setRenderParameter("resId", Long.toString(question.getActId()));
	
		actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/editquestion.jsp");
	}
	
	public void deleteanswer(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {
		
		TestAnswer answer = TestAnswerLocalServiceUtil.getTestAnswer(ParamUtil.getLong(actionRequest, "answerId"));
		TestAnswerLocalServiceUtil.deleteTestAnswer(ParamUtil.getLong(actionRequest, "answerId"));
		SessionMessages.add(actionRequest, "answer-deleted-successfully");
		actionResponse.setRenderParameter("questionId", Long.toString(answer.getQuestionId()));
		actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
		actionResponse.setRenderParameter("resId", Long.toString(TestQuestionLocalServiceUtil.getTestQuestion(answer.getQuestionId()).getActId()));
	
		actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/editquestion.jsp");
	}

	public void addanswer(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {
		
		long questionId = ParamUtil.getLong(actionRequest, "questionId");
		String answers = ParamUtil.getString(actionRequest, "answer");
		boolean correct = ParamUtil.getBoolean(actionRequest, "correct");
		String feedbackCorrect = ParamUtil.getString(actionRequest, "feedbackCorrect", "");
		String feedbackNoCorrect = ParamUtil.getString(actionRequest, "feedbackCorrect", "");
	
		if(Validator.isNull(answers)) {
			SessionErrors.add(actionRequest, "answer-test-required");
		}
		else{
			TestAnswer answer = TestAnswerLocalServiceUtil.addTestAnswer(questionId, answers, feedbackCorrect, feedbackNoCorrect, correct);
			SessionMessages.add(actionRequest, "answer-added-successfully");
		}
		
		actionResponse.setRenderParameter("questionId", Long.toString(questionId));
		actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
		actionResponse.setRenderParameter("resId", Long.toString(TestQuestionLocalServiceUtil.getTestQuestion(questionId).getActId()));

		actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/editquestion.jsp");
	}
	
	public void importSurveyQuestions(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
		
		String[] typeIds = PropsUtil.getArray("lms.questions.allowed.for.4");
		long typeId = 0;
		if (typeIds != null && typeIds.length > 0) {
			typeId = Long.valueOf(typeIds[0]);
		}
		UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(actionRequest);
		long actId = ParamUtil.getLong(actionRequest, "resId",0);
		
		String fileName = uploadRequest.getFileName("fileName");
		
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		Locale locale = themeDisplay.getLocale();
		InputStream csvFile = uploadRequest.getFileAsStream("fileName");
		if(fileName==null || StringPool.BLANK.equals(fileName)){
			SessionErrors.add(actionRequest, "surveyactivity.csverror.fileRequired");
			actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/importquestions.jsp");
		}
		else{ 
			String contentType = uploadRequest.getContentType("fileName");	
			if (!ContentTypes.TEXT_CSV.equals(contentType) && !ContentTypes.TEXT_CSV_UTF8.equals(contentType) &&!ContentTypes.APPLICATION_VND_MS_EXCEL.equals(contentType)&&!ContentTypes.APPLICATION_TEXT.equals(contentType) ) {
				SessionErrors.add(actionRequest, "surveyactivity.csvError.bad-format");	
				actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/importquestions.jsp");
			}
			else
			{
				CSVReader reader = null;
				try {
					boolean allCorrect=true;
					
					
					int line = 0;
					String questionText="";
					String[] currLine; 
					
					/*Cosas de Miguel*/
					
					byte[] buf = new byte[16384];
					String type = LiferaylmsUtil.getEncodingTypeOfFile(buf, 0, csvFile.read(buf));
					csvFile.reset();
					/*Cosas de Miguel*/		
					
					if(type.equals(LiferaylmsUtil.CHARSET_UTF_8 ) || type.equals(LiferaylmsUtil.CHARSET_UTF_16LE )|| type.equals(LiferaylmsUtil.CHARSET_UTF_32BE )
							|| type.equals(LiferaylmsUtil.CHARSET_UTF_32LE )){
						//System.out.println("UTF-8");
						reader = new CSVReader(new InputStreamReader(csvFile, StringPool.UTF8),CharPool.SEMICOLON);

					}else{
						//System.out.println("ISO");
						reader = new CSVReader(new InputStreamReader(csvFile, StringPool.ISO_8859_1),CharPool.SEMICOLON);

					}
					

					while ((currLine = reader.readNext()) != null) {
						if (line == 0) {
							line++; 
							continue; 
						}
						
						boolean correct = true;
						line++;
						if (currLine.length == 2) {
						try{
							if(currLine[0].trim().equalsIgnoreCase("")){
								SessionErrors.add(actionRequest, "surveyactivity.csvError.bad-question",line);
								correct=false;
								allCorrect=false;
							}else{
								questionText= currLine[0].trim();
							}
							String allAnswers = currLine[1].trim();
							String[] answers = allAnswers.split("\\|");
							for(String a:answers){
								if(a.equalsIgnoreCase("")){
									SessionErrors.add(actionRequest, "surveyactivity.csvError.bad-answer",line);
									correct=false;
									allCorrect=false;
									break;
								}
							}
							
							if(correct){
								TestQuestion q= TestQuestionLocalServiceUtil.addQuestion(actId, questionText, typeId);
								for(String a:answers){
									if(!a.equalsIgnoreCase("")){
										TestAnswerLocalServiceUtil.addTestAnswer(q.getQuestionId(), a, "", "",false);
									}
								}
							}
							} catch (PortalException e1) {
									e1.printStackTrace();
									SessionErrors.add(actionRequest, "surveyactivity.csvError.bad-answer",line);
									actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/importquestions.jsp");
									allCorrect=false;
								} catch (SystemException e1) {
									SessionErrors.add(actionRequest, "surveyactivity.csvError.bad-question",line);
									actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/importquestions.jsp");
									allCorrect=false;
								}
						questionText = "";
						} else {
								SessionErrors.add(actionRequest, "surveyactivity.csvError.bad-format-line",line);
								actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/importquestions.jsp");
								allCorrect=false;
						}
					}//while
					System.out.println("Se acabo");
					if(allCorrect){
						actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/editquestions.jsp");
						SessionMessages.add(actionRequest, "questions-added-successfully");
					}
	
				} catch (FileNotFoundException e) {
					SessionErrors.add(actionRequest, "surveyactivity.csvError.empty-file");
					actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/importquestions.jsp");
				} finally {
					if (reader != null) {
						reader.close();
					}
					if (!SessionErrors.isEmpty(actionRequest)){
						actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/importquestions.jsp");
					}
				}
				}
		}
		actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
		actionResponse.setRenderParameter("resId", Long.toString(actId));
	}
		
	public void editanswer(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {
		
		long answerId = ParamUtil.getLong(actionRequest, "answerId");
		String answer = ParamUtil.getString(actionRequest, "answer");
		boolean correct = ParamUtil.getBoolean(actionRequest, "correct");
		String feedbackCorrect = ParamUtil.getString(actionRequest, "feedbackCorrect", "");
		String feedbackNoCorrect = ParamUtil.getString(actionRequest, "feedbackCorrect", "");
	
		TestAnswer testanswer = TestAnswerLocalServiceUtil.getTestAnswer(answerId);
		
		if(Validator.isNull(answer)) {
			SessionErrors.add(actionRequest, "answer-test-required_"+answerId);
		}
		else{		
			testanswer.setAnswer(answer);
			testanswer.setIsCorrect(correct);
			testanswer.setFeedbackCorrect(feedbackCorrect);
			testanswer.setFeedbacknocorrect(feedbackNoCorrect);
			
			TestAnswerLocalServiceUtil.updateTestAnswer(testanswer);
			SessionMessages.add(actionRequest, "answer-added-successfully");
		}
		
		actionResponse.setRenderParameter("questionId", Long.toString(testanswer.getQuestionId()));
		actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
		actionResponse.setRenderParameter("resId", Long.toString(TestQuestionLocalServiceUtil.getTestQuestion(testanswer.getQuestionId()).getActId()));
		actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/editquestion.jsp");
	}
	
	public void deletequestion(ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {
	
		TestQuestion question = TestQuestionLocalServiceUtil.getTestQuestion(ParamUtil.getLong(actionRequest, "questionId"));
		TestQuestionLocalServiceUtil.deleteTestQuestion(ParamUtil.getLong(actionRequest, "questionId"));
	
		SessionMessages.add(actionRequest, "question-deleted-successfully");
		String backUrl = ParamUtil.get(actionRequest, "backUrl", "");
		if (Validator.isNotNull(backUrl)) {
			actionResponse.sendRedirect(backUrl);
		} else {
			QuestionType qt =new QuestionTypeRegistry().getQuestionType(question.getQuestionType());
			actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
			actionResponse.setRenderParameter("resId", Long.toString(question.getActId()));
			actionResponse.setRenderParameter("jspPage", "/html/surveyactivity/admin/edit.jsp");
		}
	}
	
	public void editactivity(ActionRequest actionRequest, ActionResponse actionResponse) throws PortalException, SystemException, Exception {
		long actId = ParamUtil.getInteger(actionRequest, "resId");
		AssetRendererFactory laf = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(LearningActivity.class.getName());
		if (laf != null) {
			AssetRenderer assetRenderer = laf.getAssetRenderer(actId, 0);

			String urlEdit = assetRenderer.getURLEdit((LiferayPortletRequest) actionRequest, (LiferayPortletResponse) actionResponse).toString();
			actionResponse.sendRedirect(urlEdit);
		}
		SessionMessages.add(actionRequest, "asset-renderer-not-defined");
	}
	

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws PortletException, IOException {
		long actId=0;
		
		if(ParamUtil.getBoolean(renderRequest, "actionEditingDetails", false)){
			
			actId=ParamUtil.getLong(renderRequest, "resId", 0);
			renderResponse.setProperty("clear-request-parameters",Boolean.TRUE.toString());
		}
		else{
			actId=ParamUtil.getLong(renderRequest, "actId", 0);
		}
					
		if(actId==0)// TODO Auto-generated method stub
		{
			renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
		}
		else
		{
				LearningActivity activity;
				try {
					activity = LearningActivityLocalServiceUtil.getLearningActivity(actId);

					//auditing
					ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
					
					long typeId=activity.getTypeId();
					
					if(typeId==4)
					{
						super.render(renderRequest, renderResponse);
					}
					else
					{
						renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
					}
				} catch (PortalException e) {
				} catch (SystemException e) {
				}			
		}
	}
	
	public void  serveResource(ResourceRequest request, ResourceResponse response)throws PortletException, IOException {

		String action = ParamUtil.getString(request, "action");
		long actId = ParamUtil.getLong(request, "resId",0);
		if(action.equals("export")){

			try {

				//Necesario para crear el fichero csv.
				response.setCharacterEncoding(StringPool.UTF8);
				response.setContentType(ContentTypes.TEXT_CSV_UTF8);
				response.addProperty(HttpHeaders.CONTENT_DISPOSITION,"attachment; fileName=data.csv");
				byte b[] = {(byte)0xEF, (byte)0xBB, (byte)0xBF};

				response.getPortletOutputStream().write(b);

				CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getPortletOutputStream(),StringPool.UTF8),CharPool.SEMICOLON);


				//Crear la cabecera con las preguntas.
				List<TestQuestion> questionsTitle = TestQuestionLocalServiceUtil.getQuestions(actId);
				List<TestQuestion> listaTotal = ListUtil.copy(questionsTitle);
				BeanComparator beanComparator = new BeanComparator("weight");
				Collections.sort(listaTotal, beanComparator);
				questionsTitle = listaTotal;
				//Anadimos x columnas para mostrar otros datos que no sean las preguntas como nombre de usuario, fecha, etc.
				int numExtraCols = 3;
				String[] cabeceras = new String[questionsTitle.size()+numExtraCols];

				//Guardamos el orden en que obtenemos las preguntas de la base de datos para poner las preguntas en el mismo orden.
				Long []questionOrder = new Long[questionsTitle.size()];

				//En las columnas extra ponemos la cabecera
				cabeceras[0]="User";
				cabeceras[1]="UserId";
				cabeceras[2]="Date";

				for(int i=numExtraCols;i<questionsTitle.size()+numExtraCols;i++){
					cabeceras[i]=formatString(questionsTitle.get(i-numExtraCols).getText())+" ("+questionsTitle.get(i-numExtraCols).getQuestionId()+")";
					questionOrder[i-numExtraCols]=questionsTitle.get(i-numExtraCols).getQuestionId();
				}
				writer.writeNext(cabeceras);

				//Partiremos del usuario para crear el csv para que sea m�s facil ver los intentos.
				List<User> users = LearningActivityTryLocalServiceUtil.getUsersByLearningActivity(actId);

				for(User user:users){

					//Para cada usuario obtenemos los intentos para la learning activity.
					List<LearningActivityTry> activities = LearningActivityTryLocalServiceUtil.getLearningActivityTryByActUser(actId, user.getUserId());
					List<Long> answersIds = new ArrayList<Long>();

					for(LearningActivityTry activity:activities){

						String xml = activity.getTryResultData();

						//Leemos el xml que contiene lo que ha respondido el estudiante.
						if(!xml.equals("")){

							Document document = SAXReaderUtil.read(xml);
							Element rootElement = document.getRootElement();

							//Obtenemos las respuestas que hay introducido.
							for(Element question:rootElement.elements("question")){

								for(Element answerElement:question.elements("answer")){
									//Guardamos el id de la respuesta para posteriormente obtener su texto.
									answersIds.add(Long.valueOf(answerElement.attributeValue("id")));
								}

							}

							//Array con los resultados de los intentos.
							String[] resultados = new String[questionOrder.length+numExtraCols];

							//Introducimos los datos de las columnas extra
							resultados[0]=user.getFullName();
							resultados[1] = String.valueOf(user.getUserId());
							resultados[2] = String.valueOf(activity.getEndDate());

							for(int i=numExtraCols;i <questionOrder.length+numExtraCols ; i++){
								//Si no tenemos respuesta para la pregunta, guardamos ""
								resultados[i] = "";

								for(int j=0;j <answersIds.size() ; j++){
									//Cuando la respuesta se corresponda con la pregunta que corresponde.
									if(Long.valueOf(getQuestionIdByAnswerId(answersIds.get(j))).compareTo(Long.valueOf(questionOrder[i-numExtraCols])) == 0){
										//Guardamos la respuesta en el array de resultados
										resultados[i]=getAnswerTextByAnswerId(answersIds.get(j));
									}
								}

							}
							//Escribimos las respuestas obtenidas para el intento en el csv.
							writer.writeNext(resultados);
						}
					}
				}

				writer.flush();
				writer.close();
				response.getPortletOutputStream().flush();
				response.getPortletOutputStream().close();

			} catch (PortalException e) {
			} catch (SystemException e) {
			} catch (DocumentException e) {
			}finally{
				response.getPortletOutputStream().flush();
				response.getPortletOutputStream().close();
			}
		} else if(action.equals("exportQuestions")){
			try {

				//Necesario para crear el fichero csv.
				String separator = "|";
				response.setCharacterEncoding(StringPool.UTF8);
				response.setContentType(ContentTypes.TEXT_CSV_UTF8);
				response.addProperty(HttpHeaders.CONTENT_DISPOSITION,"attachment; fileName=data.csv");
				byte b[] = {(byte)0xEF, (byte)0xBB, (byte)0xBF};

				response.getPortletOutputStream().write(b);

				CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getPortletOutputStream(),StringPool.UTF8),CharPool.SEMICOLON);
				
				String[] cabeceras = new String[2];
				
				//En las columnas extra ponemos la cabecera
				cabeceras[0]="Pregunta";
				cabeceras[1]="Respuestas";
				
				writer.writeNext(cabeceras);

				//Crear la cabecera con las preguntas.
				List<TestQuestion> questions = TestQuestionLocalServiceUtil.getQuestions(actId);
				List<TestQuestion> listaTotal = ListUtil.copy(questions);
				BeanComparator beanComparator = new BeanComparator("weight");
				Collections.sort(listaTotal, beanComparator);
				questions = listaTotal;

				for(TestQuestion question : questions){
					
					String[] resultados = new String[2];
					
					List<TestAnswer> answers = TestAnswerLocalServiceUtil.getTestAnswersByQuestionId(question.getQuestionId());
					//String[] answerTitles = new String[answers.size()];
					
					resultados[0] = question.getText();
					
					StringBuilder strbld = new StringBuilder();

					for(int i = 0; i < answers.size()-1; i++) {
						strbld.append(answers.get(i).getAnswer() + separator);
						//answerTitles[i] = answers.get(i).getAnswer();
						//System.out.println(answerTitles[i]);
					}
					
					strbld.append(answers.get(answers.size()-1).getAnswer());
					
					
					//resultados[1] = StringUtil.merge(answerTitles);
					resultados[1] = strbld.toString();
					
					//Escribimos las respuestas obtenidas para el intento en el csv.
					writer.writeNext(resultados);
				}

				writer.flush();
				writer.close();
				response.getPortletOutputStream().flush();
				response.getPortletOutputStream().close();

			} catch (SystemException e) {
			}finally{
				response.getPortletOutputStream().flush();
				response.getPortletOutputStream().close();
			}
		}
	}

	private String formatString(String str) {

		String res = "";

		//Jsoup elimina todas la etiquetas html del string que se le pasa, devolviendo �nicamente el texto plano.
		res = Jsoup.parse(str).text();

		//Si el texto es muy largo, lo recortamos para que sea m�s legible.
		if(res.length() > 50){
			res = res.substring(0, 50);
		}

		return res;
	}

	private Long getQuestionIdByAnswerId(Long answerId) throws PortalException, SystemException{
		//Buscamos la respuesta en el hashmap, si no lo tenemos, lo obtenemos y lo guardamos.
		if(!answersMap.containsKey(answerId))
		{
			TestAnswer answer = TestAnswerLocalServiceUtil.getTestAnswer(Long.valueOf(answerId));
			answersMap.put(answerId, answer);
		}

		return answersMap.get(answerId).getQuestionId();
	}

	private String getAnswerTextByAnswerId(Long answerId) throws PortalException, SystemException{
		//Buscamos la respuesta en el hashmap, si no lo tenemos, lo obtenemos de la bd y lo guardamos.
		if(!answersMap.containsKey(answerId))
		{
			TestAnswer answer = TestAnswerLocalServiceUtil.getTestAnswer(Long.valueOf(answerId));
			answersMap.put(answerId, answer);
		}

		return formatString(answersMap.get(answerId).getAnswer())+" ("+answersMap.get(answerId).getAnswerId()+")";
	}
}
