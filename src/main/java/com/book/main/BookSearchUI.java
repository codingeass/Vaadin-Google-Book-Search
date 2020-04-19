package com.book.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringUI
public class BookSearchUI extends UI {
	
	private static final long serialVersionUID = 1L;

	static RestTemplate restTemplate = new RestTemplate();
	
    private Grid<GBook> bookTable ;
    
    private VerticalLayout layout;
    
    private TextField filterText = new TextField();
    private Button searchButton;
    private Button clearFilter ;
    private CssLayout filterGroup;
    private HorizontalLayout buttonLayout;
    private HorizontalLayout googleApiLayout;
    
    private Resource thumbnailRes ;
    
    private TabSheet tabsheet ;
   
    @Value("${google.book.url}")
    private String googleApiURL;
    
	public void init(VaadinRequest vaadinRequest) {
		tabsheet = new TabSheet();
		VerticalLayout googleBookSection = createGoogleBookSectionUI();
		
		Label label =new Label("Google Book API");
		label.setStyleName(ValoTheme.LABEL_BOLD);
		layout = new VerticalLayout();
		layout.addComponents(label,googleBookSection);
		this.setContent(layout);
    }
	
	private VerticalLayout createGoogleBookSectionUI() {
		VerticalLayout layout = new VerticalLayout();
		
	    searchButton = new Button("Search");
	    clearFilter = new Button("Clear");
	    filterGroup=new CssLayout();
	    filterGroup.addComponents(filterText,searchButton,clearFilter);
	    filterGroup.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
	    buttonLayout = new HorizontalLayout();
	    buttonLayout.addComponents(filterGroup);
	    
	    googleApiLayout = new HorizontalLayout();
	    bookTable=new Grid<GBook>();
	    bookTable.setItems(new ArrayList<GBook>());
	    bookTable.addColumn(GBook::getTitle).setCaption("Title");
		bookTable.addColumn(GBook::getDescription).setCaption("Description");
		bookTable.addColumn(book->book.getPublicationDate()).setCaption("Publication Date");
		bookTable.addColumn(GBook::getAuthorName).setCaption("Author Name");
		Image googleThumbnail = new Image();
		googleApiLayout.addComponents(bookTable,googleThumbnail);
		googleApiLayout.setSizeFull();
		bookTable.setSizeFull();
		googleApiLayout.setExpandRatio(bookTable, 3);
		googleApiLayout.setExpandRatio(googleThumbnail, 1);
		layout.addComponents(buttonLayout ,googleApiLayout);
		
		tabsheet.addTab(layout, "Google Books");
		clearFilter.addClickListener(e->{
			filterText.setValue("");
			bookTable.setItems(new ArrayList<GBook>());
			googleThumbnail.setSource(null);
		});
		bookTable.asSingleSelect().addValueChangeListener(e -> {
			if(e.getValue() != null) {
				thumbnailRes= new ExternalResource(e.getValue().getImageUrl());
				googleThumbnail.setSource(thumbnailRes);
				googleThumbnail.setSizeFull();
			}
		});
		
		searchButton.addClickListener(e->fetchBookFromGoogle());
		return layout;
	}

	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }
	
	private void fetchBookFromGoogle() {
		try {
			InputStream is = new URL(googleApiURL+filterText.getValue().replace(" ", "+")).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			
			List<GBook> gBooks = new ArrayList<>();
			JSONArray jackson = (JSONArray)json.get("items");
			IntStream.range(0, jackson.length()).forEach(i-> {
				JSONObject jsonValue = ((JSONObject)((JSONObject)(jackson).get(i)).get("volumeInfo"));
				GBook gBook = new GBook();
				if(jsonValue.has("title"))
				{
					gBook.setTitle(jsonValue.get("title").toString());
				}
				if(jsonValue.has("description"))
				{
					gBook.setDescription(jsonValue.get("description").toString());
				}
				if(jsonValue.has("publishedDate"))
				{
					gBook.setPublicationDate(jsonValue.get("publishedDate").toString());
				}
				if(jsonValue.has("authors"))
				{
					gBook.setAuthorName((((JSONArray)jsonValue.get("authors")).get(0)).toString());
				}
				if(jsonValue.has("imageLinks"))
				{
					JSONObject image = (JSONObject)jsonValue.get("imageLinks");
					if(image.has("thumbnail"))
					{
						gBook.setImageUrl(image.get("thumbnail").toString());
					}
				}
				if(jsonValue.has("saleInfo"))
				{
					JSONObject saleInfo = (JSONObject)jsonValue.get("saleInfo");
					if(saleInfo.has("listPrice"))
					{
						if(((JSONObject)saleInfo.get("listPrice")).has("amount"))
						{
							try {
								gBook.setPrice(Double.parseDouble(((JSONObject)saleInfo.get("listPrice")).get("amount").toString()));
							}
							catch(NumberFormatException e)
							{
								//Amount has no valid value
							}
						}
					}
				}
				gBooks.add(gBook);
			});
			bookTable.setItems(gBooks);
		} catch (IOException e) {
		}
		
	}
}