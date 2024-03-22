# Sitemap Regex Extractor for Burp Suite

The Sitemap Regex Extractor is a Burp Suite extension designed to enhance your web application penetration testing workflow by enabling the search and extraction of information across your entire application's sitemap using regular expressions (regex). This powerful tool allows users to specify regex patterns for data they wish to find within the responses of all sitemap entries, providing a unique and consolidated view of potentially sensitive data, tokens, or specific identifiers that could be crucial during testing phases.

## Features

- **Regex Based Searching**: Utilize the full power of regular expressions to define complex search patterns for extracting specific information from HTTP responses.
- **Unique Extraction**: The extension ensures that the values matched by your regex are unique, avoiding duplicate entries and making your findings more manageable.

## Getting Started

To get started with the Sitemap Regex Extractor:

1. Ensure you have Burp Suite installed and running.
2. Download the Sitemap Regex Extractor jar file and import it into Burp Suite as an extension (Extender > Extensions > Add).
3. Once the extension is loaded, you will see a new tab called "Sitemap Regex Extract" added to your Burp Suite interface.
4. Enter your desired regex pattern in the provided text field. The extension specifically extracts the last grouped item in your regex pattern, allowing for precise targeting of the information you're interested in.
5. Select a base URL from the dropdown to narrow down your search or leave it at "All" to search across the entire sitemap.
6. Click the "Search" button to initiate the search process. The unique matches will be displayed in the text area below.
7. You can update the list of URLs by clicking the "Update URLs" button or cancel an ongoing search with the "Cancel" button.

## Example Usage

Suppose you want to extract all unique session IDs from the responses in your sitemap that follow a specific pattern (e.g., `\$\.ajax\(\{((\r)?)((\n)?)[ |\t]+type:(( )?)["|']\w+["|'],((\r)?)((\n)?)[ |\t]+url:(( )?)['|"](.*?)['|"],`). Simply enter this regex into the extension, and it will iterate over the sitemap entries, extracting and displaying unique matches.

## Contributing

Contributions to the Sitemap Regex Extractor are welcome! Please feel free to fork the repository, make your changes, and submit a pull request.
