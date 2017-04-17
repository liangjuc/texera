package edu.uci.ics.textdb.perftest.sample;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by junm5 on 4/10/17.
 */
public class LegalExtractionTest {
    @Test
    public void name() throws Exception {
        String  content = "IN THE UNITED STATES DISTRICT COURT FOR THE DISTRICT OF DELAWARE ANDREA L. SPOLTORE, " +
                "f/k/a ANDREA L. CADWALLADER, Plaintiff, CA. No. 04—125 (JJF) V. WILMINGTON PROFESSIONAL ASSOCIATES, INC., VVVVVVVVVVVVDefendant. NOTICE OF DEPOSITION DUCES TECUM TO: Steven T. Davis, " +
                "Esquire Oberrnayer, Rebmann, Maxwell & Hippel, LLP 3 Mill Road, Suite 306A Wilmington, " +
                "DE 19806 PLEASE TAKE NOTICE that defendant will take the deposition of Father Martin‘s Ashley* on May 27, 2005, " +
                "beginning at 10:00 am. at the offices of Obennayer Rebmann Maxwell & Hippel LLP, 20 Brace Road, Suite 300, Cherry Hill, " +
                "NJ 08034- 2634. SMITH, KATZENSTEIN & FURLOW LLP /5/ Laurence V. Cronin Laurence V. Cronin (# 2385) The Comorate Plaza 800 Delaware Avenue, PO. Box 410 Wilmington, Delaware 19899 (302) 652—8400 " +
                "Attorneys for Defendant Dated: May 12, 2005 * Attendance at the depositions will be waived if the deponents produce the documents requested pursuant to the " +
                "attached subpoena on or before May 27, 2005. {10002166.DOC} ERTIFICATE OF SERVICEQ________-.—— I HEREBY CERTIFY that on this 12th day of May 2005, I served a copy of the foregoing NOTICE OF DEPOSITION upon the following counsel of record via e-filing: Steven T. Davis, Esquire Obermayer, Rebmann, Maxwell & Hippel, LLP 3 Mill Road, Suite 306A Wilmington, DE 19806 /s/ Laurence V. Cronin Laurence V. Cronin (ID 2385) 10002355.WPD";

        String regex = "\\b[a-zA-Z,. /]{0,200}, Plaintiff\\b";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        System.out.println();

        while (matcher.find()){
            System.out.println(matcher.group());
        }

    }
}