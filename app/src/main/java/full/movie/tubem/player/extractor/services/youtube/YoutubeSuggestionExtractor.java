package full.movie.tubem.player.extractor.services.youtube;

import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.SuggestionExtractor;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class YoutubeSuggestionExtractor extends SuggestionExtractor {
    public static final String CHARSET_UTF_8 = "UTF-8";

    public YoutubeSuggestionExtractor(int serviceId) {
        super(serviceId);
    }

    public List<String> suggestionList(String query, String contentCountry) throws ExtractionException, IOException {
        List<String> suggestions = new ArrayList<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(Newapp.getDownloader().download("https://suggestqueries.google.com/complete/search?client=&output=toolbar&ds=yt&hl=" + URLEncoder.encode(contentCountry, "UTF-8") + "&q=" + URLEncoder.encode(query, "UTF-8")).getBytes("UTF-8"))));
            doc.getDocumentElement().normalize();
            try {
                NodeList nList = doc.getElementsByTagName("CompleteSuggestion");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode1 = doc.getElementsByTagName("suggestion").item(temp);
                    if (nNode1.getNodeType() == 1) {
                        suggestions.add(((Element) nNode1).getAttribute("data"));
                    }
                }
                return suggestions;
            } catch (Exception e) {
                throw new ParsingException("Could not get suggestions form document.", e);
            }
        } catch (IOException | ParserConfigurationException | SAXException e2) {
            throw new ParsingException("Could not parse document.");
        }
    }
}
