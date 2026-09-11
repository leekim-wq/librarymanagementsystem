package com.library.config;

import com.library.model.Book;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Autowired private MemberRepository memberRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        loadDefaultUsers();
        loadSampleBooks();
    }

    // ============================================================
    // DEFAULT USERS
    // ============================================================
    private void loadDefaultUsers() {
        if (memberRepository.count() == 0) {
            log.info("Creating default members...");

            Member admin = new Member();
            admin.setUsername("admin");
            admin.setEmail("admin@library.com");
            admin.setName("System Administrator");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            admin.setMembershipDate(LocalDate.now());
            admin.setBorrowingLimit(10);
            admin.setTotalFines(0.0);
            memberRepository.save(admin);

            Member librarian = new Member();
            librarian.setUsername("librarian");
            librarian.setEmail("librarian@library.com");
            librarian.setName("Head Librarian");
            librarian.setPassword(passwordEncoder.encode("lib123"));
            librarian.setRole("LIBRARIAN");
            librarian.setActive(true);
            librarian.setMembershipDate(LocalDate.now());
            librarian.setBorrowingLimit(10);
            librarian.setTotalFines(0.0);
            memberRepository.save(librarian);

            Member member = new Member();
            member.setUsername("member");
            member.setEmail("member@library.com");
            member.setName("Regular Member");
            member.setPassword(passwordEncoder.encode("member123"));
            member.setRole("MEMBER");
            member.setActive(true);
            member.setMembershipDate(LocalDate.now());
            member.setBorrowingLimit(5);
            member.setTotalFines(0.0);
            memberRepository.save(member);

            log.info("Default members created: admin / librarian / member");
        }
    }

    // ============================================================
    // SAMPLE BOOKS — 200+ real titles
    // ============================================================
    private void loadSampleBooks() {
        if (bookRepository.count() > 0) {
            log.info("Books already loaded: {}", bookRepository.count());
            return;
        }

        log.info("Loading 200+ books...");
        List<Book> books = new ArrayList<>();

        // ============================================================
        // PROGRAMMING — CORE (30)
        // ============================================================
        books.add(b("Clean Code", "Robert C. Martin", "Programming", 4.9, 45,
                "A handbook of agile software craftsmanship."));
        books.add(b("The Pragmatic Programmer", "David Thomas", "Programming", 4.8, 38,
                "Your journey to mastery."));
        books.add(b("Code Complete", "Steve McConnell", "Programming", 4.8, 30,
                "A practical handbook of software construction."));
        books.add(b("The Clean Coder", "Robert C. Martin", "Programming", 4.6, 25,
                "A code of conduct for professional programmers."));
        books.add(b("Refactoring", "Martin Fowler", "Programming", 4.7, 28,
                "Improving the design of existing code."));
        books.add(b("Working Effectively with Legacy Code", "Michael Feathers", "Programming", 4.6, 22,
                "Techniques for working with legacy systems."));
        books.add(b("The Mythical Man-Month", "Frederick P. Brooks", "Programming", 4.4, 20,
                "Essays on software engineering."));
        books.add(b("Structure and Interpretation of Computer Programs", "Harold Abelson", "Programming", 4.6, 15,
                "Classic CS textbook."));
        books.add(b("The Art of Computer Programming Vol. 1", "Donald Knuth", "Programming", 4.9, 12,
                "Fundamental algorithms."));
        books.add(b("Introduction to Algorithms", "Thomas H. Cormen", "Programming", 4.5, 25,
                "Comprehensive algorithms textbook."));

        books.add(b("Effective Java", "Joshua Bloch", "Java Programming", 4.8, 40,
                "Best practices for the Java platform."));
        books.add(b("Java: The Complete Reference", "Herbert Schildt", "Java Programming", 4.5, 35,
                "Comprehensive Java reference."));
        books.add(b("Head First Java", "Kathy Sierra", "Java Programming", 4.6, 38,
                "A brain-friendly guide to Java."));
        books.add(b("Java Concurrency in Practice", "Brian Goetz", "Java Programming", 4.7, 20,
                "Concurrency best practices."));
        books.add(b("Spring in Action", "Craig Walls", "Java Programming", 4.4, 18,
                "Building Spring applications."));
        books.add(b("Java Performance", "Scott Oaks", "Java Programming", 4.2, 15,
                "Definitive guide to Java performance."));
        books.add(b("Thinking in Java", "Bruce Eckel", "Java Programming", 4.5, 22,
                "The definitive introduction to Java."));
        books.add(b("Java: A Beginner's Guide", "Herbert Schildt", "Java Programming", 4.3, 30,
                "Beginner-friendly Java guide."));
        books.add(b("OCA Java SE 8 Programmer I", "Kathy Sierra", "Java Programming", 4.4, 18,
                "Certification study guide."));
        books.add(b("Modern Java in Action", "Raoul-Gabriel Urma", "Java Programming", 4.6, 20,
                "Lambdas, streams, and functional programming."));

        books.add(b("Python Crash Course", "Eric Matthes", "Python Programming", 4.7, 45,
                "A hands-on, project-based introduction."));
        books.add(b("Automate the Boring Stuff with Python", "Al Sweigart", "Python Programming", 4.8, 50,
                "Practical programming for total beginners."));
        books.add(b("Fluent Python", "Luciano Ramalho", "Python Programming", 4.6, 28,
                "Clear, concise, and effective Python."));
        books.add(b("Effective Python", "Brett Slatkin", "Python Programming", 4.4, 22,
                "90 specific ways to write better Python."));
        books.add(b("Python for Data Analysis", "Wes McKinney", "Python Programming", 4.5, 25,
                "Data wrangling with pandas, NumPy, and IPython."));
        books.add(b("Dive Into Python 3", "Mark Pilgrim", "Python Programming", 4.3, 18,
                "A guide to Python 3."));
        books.add(b("Learning Python", "Mark Lutz", "Python Programming", 4.4, 25,
                "Powerful object-oriented programming."));
        books.add(b("Python Cookbook", "David Beazley", "Python Programming", 4.5, 20,
                "Recipes for mastering Python 3."));
        books.add(b("The Quick Python Book", "Naomi Ceder", "Python Programming", 4.2, 15,
                "Fast-paced Python guide."));
        books.add(b("Python Tricks", "Dan Bader", "Python Programming", 4.3, 18,
                "A buffet of awesome Python features."));

        // ============================================================
        // WEB DEVELOPMENT (25)
        // ============================================================
        books.add(b("HTML and CSS: Design and Build Websites", "Jon Duckett", "Web Development", 4.5, 40,
                "A full-color introduction to HTML and CSS."));
        books.add(b("JavaScript: The Good Parts", "Douglas Crockford", "Web Development", 4.4, 28,
                "Unearthing the excellence in JavaScript."));
        books.add(b("Eloquent JavaScript", "Marijn Haverbeke", "Web Development", 4.6, 30,
                "A modern introduction to programming."));
        books.add(b("You Don't Know JS: Up & Going", "Kyle Simpson", "Web Development", 4.3, 25,
                "JavaScript fundamentals."));
        books.add(b("JavaScript: The Definitive Guide", "David Flanagan", "Web Development", 4.5, 28,
                "The definitive JavaScript reference."));
        books.add(b("Learning React", "Alex Banks", "Web Development", 4.4, 25,
                "Modern patterns for developing React apps."));
        books.add(b("React Up & Running", "Stoyan Stefanov", "Web Development", 4.2, 18,
                "Building web applications with React."));
        books.add(b("Fullstack React", "Anthony Accomazzo", "Web Development", 4.3, 20,
                "The complete guide to ReactJS."));
        books.add(b("Node.js Design Patterns", "Mario Casciaro", "Web Development", 4.4, 22,
                "Design and implement production-grade Node.js apps."));
        books.add(b("Node.js in Action", "Mike Cantelon", "Web Development", 4.2, 18,
                "Building Node.js applications."));

        books.add(b("Learning Vue.js", "Olga Filipova", "Web Development", 4.3, 20,
                "Building modern web apps with Vue.js."));
        books.add(b("Vue.js Up and Running", "Callum Macrae", "Web Development", 4.2, 16,
                "Building accessible and performant web apps."));
        books.add(b("Angular Up and Running", "Shyam Seshadri", "Web Development", 4.1, 14,
                "Learning Angular, step by step."));
        books.add(b("Django for Beginners", "William S. Vincent", "Web Development", 4.4, 22,
                "Building web applications with Django."));
        books.add(b("Flask Web Development", "Miguel Grinberg", "Web Development", 4.3, 20,
                "Developing web applications with Python."));
        books.add(b("Two Scoops of Django", "Daniel Roy Greenfeld", "Web Development", 4.5, 18,
                "Best practices for Django 2.x."));
        books.add(b("Rails Tutorial", "Michael Hartl", "Web Development", 4.5, 25,
                "Learn Web Development with Rails."));
        books.add(b("Pro Git", "Scott Chacon", "Web Development", 4.6, 30,
                "Everything you need to know about Git."));
        books.add(b("Learning Web Design", "Jennifer Robbins", "Web Development", 4.4, 22,
                "A beginner's guide to HTML, CSS, JavaScript."));
        books.add(b("CSS: The Definitive Guide", "Eric A. Meyer", "Web Development", 4.5, 20,
                "The definitive CSS reference."));

        books.add(b("Don't Make Me Think", "Steve Krug", "Web Development", 4.6, 28,
                "A common sense approach to web usability."));
        books.add(b("Web Design with HTML, CSS, JavaScript", "Jon Duckett", "Web Development", 4.4, 25,
                "A comprehensive intro to web design."));
        books.add(b("JavaScript and JQuery", "Jon Duckett", "Web Development", 4.5, 28,
                "Interactive front-end web development."));
        books.add(b("Front-End Tooling with Gulp, Bower, and Yeoman", "Stefan Baumgartner", "Web Development", 4.2, 12,
                "Modern front-end workflow tools."));
        books.add(b("Responsive Web Design", "Ethan Marcotte", "Web Development", 4.3, 16,
                "Designing for the modern web."));

        // ============================================================
        // DATA SCIENCE / AI / ML (25)
        // ============================================================
        books.add(b("Introduction to Machine Learning with Python", "Andreas C. Müller", "Data Science", 4.5, 25,
                "A guide for data scientists."));
        books.add(b("Deep Learning", "Ian Goodfellow", "Data Science", 4.6, 20,
                "The definitive deep learning textbook."));
        books.add(b("Pattern Recognition and Machine Learning", "Christopher Bishop", "Data Science", 4.4, 18,
                "Classic machine learning reference."));
        books.add(b("The Elements of Statistical Learning", "Trevor Hastie", "Data Science", 4.5, 15,
                "Data mining, inference, and prediction."));
        books.add(b("Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow", "Aurélien Géron", "Data Science", 4.8, 28,
                "Practical machine learning guide."));
        books.add(b("Python Machine Learning", "Sebastian Raschka", "Data Science", 4.5, 22,
                "Machine learning with Python."));
        books.add(b("Data Science from Scratch", "Joel Grus", "Data Science", 4.4, 20,
                "First principles with Python."));
        books.add(b("Deep Learning with Python", "François Chollet", "Data Science", 4.5, 20,
                "Deep learning with Keras."));
        books.add(b("Machine Learning Yearning", "Andrew Ng", "Data Science", 4.6, 18,
                "Technical strategy for AI engineers."));
        books.add(b("Natural Language Processing with Python", "Steven Bird", "Data Science", 4.3, 15,
                "Analyzing text with the Natural Language Toolkit."));

        books.add(b("Artificial Intelligence: A Modern Approach", "Stuart Russell", "AI", 4.7, 22,
                "The definitive AI textbook."));
        books.add(b("Reinforcement Learning: An Introduction", "Richard S. Sutton", "AI", 4.5, 14,
                "Introduction to reinforcement learning."));
        books.add(b("Machine Learning: A Probabilistic Perspective", "Kevin Murphy", "AI", 4.3, 12,
                "Probabilistic approach to ML."));
        books.add(b("Computer Vision: Algorithms and Applications", "Richard Szeliski", "AI", 4.2, 12,
                "Computer vision fundamentals."));
        books.add(b("Speech and Language Processing", "Daniel Jurafsky", "AI", 4.4, 14,
                "Introduction to NLP and speech recognition."));
        books.add(b("Bayesian Reasoning and Machine Learning", "David Barber", "AI", 4.3, 10,
                "Bayesian approach to ML."));
        books.add(b("The Hundred-Page Machine Learning Book", "Andriy Burkov", "AI", 4.6, 25,
                "Concise introduction to ML."));
        books.add(b("Grokking Deep Learning", "Andrew Trask", "AI", 4.5, 18,
                "Deep learning illustrated."));
        books.add(b("Grokking Machine Learning", "Luis Serrano", "AI", 4.6, 20,
                "Machine learning illustrated."));
        books.add(b("Practical Statistics for Data Scientists", "Peter Bruce", "Data Science", 4.5, 18,
                "50+ essential concepts using R and Python."));

        books.add(b("Storytelling with Data", "Cole Nussbaumer Knaflic", "Data Science", 4.6, 22,
                "A data visualization guide."));
        books.add(b("The Visual Display of Quantitative Information", "Edward Tufte", "Data Science", 4.7, 15,
                "Classic book on data visualization."));
        books.add(b("Data Science for Business", "Foster Provost", "Data Science", 4.4, 18,
                "What you need to know about data mining."));
        books.add(b("R for Data Science", "Hadley Wickham", "Data Science", 4.6, 20,
                "Import, tidy, transform, visualize, and model data."));
        books.add(b("Python Data Science Handbook", "Jake VanderPlas", "Data Science", 4.6, 22,
                "Essential tools for working with data."));

        // ============================================================
        // COMPUTER SCIENCE FUNDAMENTALS (20)
        // ============================================================
        books.add(b("Computer Science: An Overview", "J. Glenn Brookshear", "Computer Science", 4.2, 20,
                "A broad introduction to computer science."));
        books.add(b("Operating System Concepts", "Abraham Silberschatz", "Computer Science", 4.3, 18,
                "The dinosaur book of operating systems."));
        books.add(b("Modern Operating Systems", "Andrew S. Tanenbaum", "Computer Science", 4.5, 16,
                "Comprehensive OS textbook."));
        books.add(b("Computer Networks", "Andrew S. Tanenbaum", "Computer Science", 4.4, 16,
                "Classic networking textbook."));
        books.add(b("Database Management Systems", "Raghu Ramakrishnan", "Computer Science", 4.2, 15,
                "Database fundamentals."));
        books.add(b("Database System Concepts", "Abraham Silberschatz", "Computer Science", 4.3, 15,
                "Comprehensive database textbook."));
        books.add(b("Software Engineering", "Ian Sommerville", "Computer Science", 4.5, 22,
                "Comprehensive software engineering textbook."));
        books.add(b("Compilers: Principles, Techniques, and Tools", "Alfred Aho", "Computer Science", 4.3, 12,
                "The dragon book."));
        books.add(b("Digital Design and Computer Architecture", "David Harris", "Computer Science", 4.2, 12,
                "Digital design fundamentals."));
        books.add(b("Computer Organization and Design", "David Patterson", "Computer Science", 4.4, 14,
                "The hardware/software interface."));

        books.add(b("Introduction to the Theory of Computation", "Michael Sipser", "Computer Science", 4.3, 12,
                "Theory of computation fundamentals."));
        books.add(b("Discrete Mathematics and Its Applications", "Kenneth Rosen", "Computer Science", 4.4, 15,
                "Discrete math for CS."));
        books.add(b("Concrete Mathematics", "Ronald Graham", "Computer Science", 4.3, 10,
                "A foundation for computer science."));
        books.add(b("Linear Algebra and Its Applications", "Gilbert Strang", "Computer Science", 4.5, 12,
                "Linear algebra fundamentals."));
        books.add(b("Probability and Statistics for Engineering", "Jay Devore", "Computer Science", 4.0, 10,
                "Probability and statistics."));
        books.add(b("Distributed Systems", "Maarten van Steen", "Computer Science", 4.2, 10,
                "Principles and paradigms."));
        books.add(b("Designing Data-Intensive Applications", "Martin Kleppmann", "Computer Science", 4.8, 25,
                "The big ideas behind reliable, scalable systems."));
        books.add(b("The Linux Programming Interface", "Michael Kerrisk", "Computer Science", 4.7, 12,
                "A Linux and UNIX system programming handbook."));
        books.add(b("Programming Pearls", "Jon Bentley", "Computer Science", 4.5, 14,
                "Classic programming wisdom."));
        books.add(b("The Algorithm Design Manual", "Steven Skiena", "Computer Science", 4.7, 15,
                "A practical guide to algorithm design."));

        // ============================================================
        // ARCHITECTURE / DESIGN PATTERNS (15)
        // ============================================================
        books.add(b("Design Patterns", "Erich Gamma", "Architecture", 4.7, 32,
                "Elements of reusable object-oriented software."));
        books.add(b("Head First Design Patterns", "Eric Freeman", "Architecture", 4.6, 35,
                "A brain-friendly guide to design patterns."));
        books.add(b("Patterns of Enterprise Application Architecture", "Martin Fowler", "Architecture", 4.4, 18,
                "Enterprise application patterns."));
        books.add(b("Domain-Driven Design", "Eric Evans", "Architecture", 4.6, 22,
                "Tackling complexity in the heart of software."));
        books.add(b("Implementing Domain-Driven Design", "Vaughn Vernon", "Architecture", 4.5, 16,
                "Practical DDD techniques."));
        books.add(b("Clean Architecture", "Robert C. Martin", "Architecture", 4.7, 30,
                "A craftsman's guide to software structure and design."));
        books.add(b("Building Microservices", "Sam Newman", "Architecture", 4.6, 22,
                "Designing fine-grained systems."));
        books.add(b("Monolith to Microservices", "Sam Newman", "Architecture", 4.5, 18,
                "Evolutionary patterns to transform your monolith."));
        books.add(b("Software Architecture in Practice", "Len Bass", "Architecture", 4.3, 12,
                "Practical software architecture."));
        books.add(b("Fundamentals of Software Architecture", "Mark Richards", "Architecture", 4.6, 20,
                "An engineering approach."));
        books.add(b("The Software Architect Elevator", "Gregor Hohpe", "Architecture", 4.5, 12,
                "Redefining the architect's role."));
        books.add(b("Enterprise Integration Patterns", "Gregor Hohpe", "Architecture", 4.5, 14,
                "Designing, building, and deploying messaging solutions."));
        books.add(b("Release It!", "Michael T. Nygard", "Architecture", 4.7, 18,
                "Design and deploy production-ready software."));
        books.add(b("The Phoenix Project", "Gene Kim", "Architecture", 4.7, 25,
                "A novel about IT, DevOps, and helping your business win."));
        books.add(b("The DevOps Handbook", "Gene Kim", "Architecture", 4.6, 20,
                "How to create world-class agility, reliability, and security."));

        // ============================================================
        // CYBERSECURITY (15)
        // ============================================================
        books.add(b("The Web Application Hacker's Handbook", "Dafydd Stuttard", "Cybersecurity", 4.5, 18,
                "Finding and exploiting security flaws."));
        books.add(b("Hacking: The Art of Exploitation", "Jon Erickson", "Cybersecurity", 4.4, 16,
                "Hacking fundamentals and techniques."));
        books.add(b("Practical Malware Analysis", "Michael Sikorski", "Cybersecurity", 4.4, 12,
                "The hands-on guide to dissecting malicious software."));
        books.add(b("The Tangled Web", "Michal Zalewski", "Cybersecurity", 4.2, 10,
                "A guide to securing modern web applications."));
        books.add(b("Metasploit", "David Kennedy", "Cybersecurity", 4.3, 14,
                "The penetration tester's guide."));
        books.add(b("The Art of Intrusion", "Kevin D. Mitnick", "Cybersecurity", 4.3, 12,
                "Real stories from the world's most notorious hackers."));
        books.add(b("The Art of Deception", "Kevin D. Mitnick", "Cybersecurity", 4.4, 12,
                "Controlling the human element of security."));
        books.add(b("Ghost in the Wires", "Kevin D. Mitnick", "Cybersecurity", 4.5, 14,
                "My adventures as the world's most wanted hacker."));
        books.add(b("Countdown to Zero Day", "Kim Zetter", "Cybersecurity", 4.5, 10,
                "Stuxnet and the launch of the world's first digital weapon."));
        books.add(b("Cybersecurity and Cyberwar", "P.W. Singer", "Cybersecurity", 4.3, 12,
                "What everyone needs to know."));
        books.add(b("The Cyber Effect", "Mary Aiken", "Cybersecurity", 4.2, 8,
                "A pioneering cyberpsychologist explains how human behavior changes online."));
        books.add(b("Click Here to Kill Everybody", "Bruce Schneier", "Cybersecurity", 4.4, 10,
                "Security and survival in a hyper-connected world."));
        books.add(b("Data and Goliath", "Bruce Schneier", "Cybersecurity", 4.5, 12,
                "The hidden battles to collect your data and control your world."));
        books.add(b("The Hacker Playbook 3", "Peter Kim", "Cybersecurity", 4.5, 14,
                "Practical guide to penetration testing."));
        books.add(b("Red Team Field Manual", "Ben Clark", "Cybersecurity", 4.4, 15,
                "A concise reference for red team operators."));

        // ============================================================
        // DEVOPS / CLOUD (15)
        // ============================================================
        books.add(b("Docker: Up & Running", "Karl Matthias", "DevOps", 4.3, 18,
                "Shipping reliable containers in production."));
        books.add(b("Kubernetes: Up and Running", "Kelsey Hightower", "DevOps", 4.4, 16,
                "Dive into the future of infrastructure."));
        books.add(b("Kubernetes Patterns", "Bilgin Ibryam", "DevOps", 4.5, 14,
                "Reusable elements for designing cloud-native applications."));
        books.add(b("Terraform: Up & Running", "Yevgeniy Brikman", "DevOps", 4.6, 15,
                "Writing infrastructure as code."));
        books.add(b("Ansible: Up and Running", "Lorin Hochstein", "DevOps", 4.3, 12,
                "Automating configuration management and deployment."));
        books.add(b("Continuous Delivery", "Jez Humble", "DevOps", 4.6, 15,
                "Reliable software releases through build, test, and deployment automation."));
        books.add(b("Accelerate", "Nicole Forsgren", "DevOps", 4.6, 18,
                "The science of lean software and DevOps."));
        books.add(b("Site Reliability Engineering", "Betsy Beyer", "DevOps", 4.7, 15,
                "How Google runs production systems."));
        books.add(b("The Site Reliability Workbook", "Betsy Beyer", "DevOps", 4.6, 12,
                "Practical ways to implement SRE."));
        books.add(b("AWS Certified Solutions Architect", "David Clinton", "Cloud", 4.3, 15,
                "AWS certification study guide."));
        books.add(b("AWS Certified Developer", "Jon Bonso", "Cloud", 4.4, 14,
                "AWS developer certification."));
        books.add(b("Cloud Native Patterns", "Cornelia Davis", "Cloud", 4.5, 12,
                "Designing change-tolerant software."));
        books.add(b("Cloud Native DevOps with Kubernetes", "John Arundel", "Cloud", 4.4, 12,
                "Building, deploying, and scaling modern applications."));
        books.add(b("Google Cloud Platform in Action", "JJ Geewax", "Cloud", 4.3, 10,
                "Practical GCP guide."));
        books.add(b("Microsoft Azure Essentials", "Michael Collier", "Cloud", 4.2, 10,
                "Azure fundamentals."));

        // ============================================================
        // C / C++ / C# / OTHER LANGUAGES (20)
        // ============================================================
        books.add(b("The C Programming Language", "Brian W. Kernighan", "C Programming", 4.7, 30,
                "The classic C reference."));
        books.add(b("C Programming: A Modern Approach", "K.N. King", "C Programming", 4.6, 18,
                "Comprehensive C programming."));
        books.add(b("C++ Primer", "Stanley B. Lippman", "C++ Programming", 4.5, 22,
                "The definitive C++ primer."));
        books.add(b("Effective C++", "Scott Meyers", "C++ Programming", 4.7, 18,
                "55 specific ways to improve your programs."));
        books.add(b("Effective Modern C++", "Scott Meyers", "C++ Programming", 4.7, 18,
                "42 specific ways to improve your use of C++11 and C++14."));
        books.add(b("The C++ Programming Language", "Bjarne Stroustrup", "C++ Programming", 4.5, 16,
                "The definitive C++ reference."));
        books.add(b("C# in Depth", "Jon Skeet", "C# Programming", 4.6, 18,
                "Master the subtleties of C#."));
        books.add(b("CLR via C#", "Jeffrey Richter", "C# Programming", 4.7, 14,
                "Deep dive into the .NET CLR."));
        books.add(b("Pro C# 10 with .NET 6", "Andrew Troelsen", "C# Programming", 4.5, 16,
                "Foundational principles and practices."));
        books.add(b("Programming C# 8.0", "Ian Griffiths", "C# Programming", 4.5, 14,
                "Build cloud, web, and desktop applications."));

        books.add(b("Programming Rust", "Jim Blandy", "Systems Programming", 4.6, 14,
                "Fast and safe systems programming."));
        books.add(b("The Rust Programming Language", "Steve Klabnik", "Systems Programming", 4.7, 20,
                "The official Rust book."));
        books.add(b("Programming Go", "Alan A. A. Donovan", "Systems Programming", 4.6, 16,
                "Building reliable, scalable systems with Go."));
        books.add(b("The Go Programming Language", "Alan A. A. Donovan", "Systems Programming", 4.7, 18,
                "The definitive Go reference."));
        books.add(b("Learning Go", "Jon Bodner", "Systems Programming", 4.5, 14,
                "Idiomatic Go."));
        books.add(b("Programming Elixir", "Dave Thomas", "Systems Programming", 4.6, 12,
                "Functional, concurrent, pragmatic."));
        books.add(b("Programming Erlang", "Joe Armstrong", "Systems Programming", 4.5, 10,
                "Software for a concurrent world."));
        books.add(b("Programming Ruby", "Dave Thomas", "Systems Programming", 4.4, 12,
                "The pragmatic programmer's guide."));
        books.add(b("Programming Perl", "Larry Wall", "Systems Programming", 4.2, 10,
                "The camel book."));
        books.add(b("Programming PHP", "Kevin Tatroe", "Systems Programming", 4.3, 12,
                "Creating dynamic web pages."));

        // ============================================================
        // GAME DEVELOPMENT (10)
        // ============================================================
        books.add(b("Game Programming Patterns", "Robert Nystrom", "Game Development", 4.7, 18,
                "Classic patterns for game developers."));
        books.add(b("The Art of Game Design", "Jesse Schell", "Game Development", 4.6, 15,
                "A book of lenses."));
        books.add(b("Game Engine Architecture", "Jason Gregory", "Game Development", 4.5, 12,
                "Comprehensive game engine design."));
        books.add(b("Real-Time Rendering", "Tomas Akenine-Möller", "Game Development", 4.7, 10,
                "Real-time computer graphics."));
        books.add(b("Unity in Action", "Joseph Hocking", "Game Development", 4.5, 15,
                "Multiplatform game development in C#."));
        books.add(b("Learning Unreal Engine Game Development", "Joanna Lee", "Game Development", 4.3, 12,
                "Practical Unreal Engine guide."));
        books.add(b("Godot Engine Game Development", "Chris Bradfield", "Game Development", 4.5, 12,
                "Build games with the Godot engine."));
        books.add(b("Level Up!", "Scott Rogers", "Game Development", 4.4, 10,
                "The guide to great video game design."));
        books.add(b("The Ultimate Guide to Video Game Writing and Design", "Flint Dille", "Game Development", 4.3, 8,
                "Writing and design for games."));
        books.add(b("Blood, Sweat, and Pixels", "Jason Schreier", "Game Development", 4.6, 14,
                "The triumphant, turbulent stories behind how video games are made."));

        // ============================================================
        // MOBILE DEVELOPMENT (10)
        // ============================================================
        books.add(b("Android Programming: The Big Nerd Ranch Guide", "Bill Phillips", "Mobile Development", 4.4, 18,
                "Comprehensive Android guide."));
        books.add(b("Kotlin Programming: The Big Nerd Ranch Guide", "Josh Skeen", "Mobile Development", 4.5, 14,
                "Learn Kotlin programming."));
        books.add(b("iOS Programming: The Big Nerd Ranch Guide", "Christian Keur", "Mobile Development", 4.4, 16,
                "Comprehensive iOS guide."));
        books.add(b("Swift Programming: The Big Nerd Ranch Guide", "Matthew Mathias", "Mobile Development", 4.5, 14,
                "Learn Swift programming."));
        books.add(b("Flutter in Action", "Eric Windmill", "Mobile Development", 4.5, 15,
                "Building cross-platform apps with Flutter."));
        books.add(b("React Native in Action", "Nader Dabit", "Mobile Development", 4.4, 14,
                "Building mobile apps with React Native."));
        books.add(b("Head First Android Development", "Dawn Griffiths", "Mobile Development", 4.3, 16,
                "A brain-friendly guide to Android."));
        books.add(b("Head First iPhone and iPad Development", "Dan Pilone", "Mobile Development", 4.2, 10,
                "A learner's guide to iOS."));
        books.add(b("Learning Swift", "Jon Manning", "Mobile Development", 4.4, 12,
                "Building apps for macOS, iOS, and beyond."));
        books.add(b("Android Programming for Beginners", "John Horton", "Mobile Development", 4.3, 14,
                "Learn all the Java and Android skills you need."));

        // ============================================================
        // TESTING / QA (10)
        // ============================================================
        books.add(b("Test Driven Development: By Example", "Kent Beck", "Testing", 4.6, 18,
                "The definitive TDD guide."));
        books.add(b("Growing Object-Oriented Software, Guided by Tests", "Steve Freeman", "Testing", 4.6, 14,
                "TDD in practice."));
        books.add(b("Working Effectively with Unit Tests", "Jay Fields", "Testing", 4.3, 10,
                "A comprehensive guide to writing effective unit tests."));
        books.add(b("The Art of Unit Testing", "Roy Osherove", "Testing", 4.5, 14,
                "With examples in C#."));
        books.add(b("xUnit Test Patterns", "Gerard Meszaros", "Testing", 4.5, 12,
                "Refactoring test code."));
        books.add(b("Continuous Testing", "Daniel S. Brekke", "Testing", 4.2, 8,
                "A practical guide."));
        books.add(b("More Agile Testing", "Janet Gregory", "Testing", 4.4, 10,
                "Learning journeys and agile testing."));
        books.add(b("Agile Testing", "Lisa Crispin", "Testing", 4.4, 12,
                "A practical guide for testers and agile teams."));
        books.add(b("Explore It!", "Elisabeth Hendrickson", "Testing", 4.5, 10,
                "Reduce risk and increase confidence with exploratory testing."));
        books.add(b("Specification by Example", "Gojko Adzic", "Testing", 4.5, 10,
                "How successful teams deliver the right software."));

        // ============================================================
        // CAREER / SOFT SKILLS (15)
        // ============================================================
        books.add(b("The Effective Engineer", "Edmond Lau", "Career", 4.5, 22,
                "How to leverage your efforts in software engineering."));
        books.add(b("Soft Skills: The Software Developer's Life Manual", "John Sonmez", "Career", 4.3, 18,
                "Career and life for software developers."));
        books.add(b("Cracking the Coding Interview", "Gayle Laakmann McDowell", "Career", 4.6, 30,
                "189 programming questions and solutions."));
        books.add(b("Elements of Programming Interviews", "Adnan Aziz", "Career", 4.5, 18,
                "The insider's guide."));
        books.add(b("Programming Interviews Exposed", "John Mongan", "Career", 4.3, 16,
                "Secrets to landing your next job."));
        books.add(b("The Google Resume", "Gayle Laakmann McDowell", "Career", 4.4, 12,
                "How to prepare for a career at Google, Microsoft, Apple, or any top tech company."));
        books.add(b("Peopleware", "Tom DeMarco", "Career", 4.4, 15,
                "Productive projects and teams."));
        books.add(b("Managing Humans", "Michael Lopp", "Career", 4.3, 12,
                "Biting and humorous tales of a software engineering manager."));
        books.add(b("The Manager's Path", "Camille Fournier", "Career", 4.6, 14,
                "A guide for tech leaders navigating growth and change."));
        books.add(b("An Elegant Puzzle", "Will Larson", "Career", 4.5, 12,
                "Systems of engineering management."));
        books.add(b("The Staff Engineer's Path", "Tanya Reilly", "Career", 4.7, 14,
                "A guide for individual contributors navigating growth and change."));
        books.add(b("Staff Engineer", "Will Larson", "Career", 4.6, 12,
                "Leadership beyond the management track."));
        books.add(b("Atomic Habits", "James Clear", "Career", 4.8, 35,
                "An easy and proven way to build good habits."));
        books.add(b("Deep Work", "Cal Newport", "Career", 4.6, 28,
                "Rules for focused success in a distracted world."));
        books.add(b("So Good They Can't Ignore You", "Cal Newport", "Career", 4.5, 20,
                "Why skills trump passion in the quest for work you love."));

        // ============================================================
        // BUSINESS / STARTUP (10)
        // ============================================================
        books.add(b("The Lean Startup", "Eric Ries", "Business", 4.5, 22,
                "How today's entrepreneurs use continuous innovation."));
        books.add(b("Zero to One", "Peter Thiel", "Business", 4.5, 25,
                "Notes on startups, or how to build the future."));
        books.add(b("The Hard Thing About Hard Things", "Ben Horowitz", "Business", 4.7, 20,
                "Building a business when there are no easy answers."));
        books.add(b("Good to Great", "Jim Collins", "Business", 4.5, 18,
                "Why some companies make the leap and others don't."));
        books.add(b("The Innovator's Dilemma", "Clayton M. Christensen", "Business", 4.4, 12,
                "When new technologies cause great firms to fail."));
        books.add(b("Crossing the Chasm", "Geoffrey A. Moore", "Business", 4.4, 12,
                "Marketing and selling disruptive products."));
        books.add(b("The Mom Test", "Rob Fitzpatrick", "Business", 4.6, 15,
                "How to talk to customers and learn if your business is a good idea."));
        books.add(b("Running Lean", "Ash Maurya", "Business", 4.5, 14,
                "Iterate from plan A to a plan that works."));
        books.add(b("The Startup Owner's Manual", "Steve Blank", "Business", 4.4, 10,
                "The step-by-step guide for building a great company."));
        books.add(b("Business Model Generation", "Alexander Osterwalder", "Business", 4.5, 12,
                "A handbook for visionaries, game changers, and challengers."));

        // ============================================================
        // PHILOSOPHY / HISTORY OF COMPUTING (10)
        // ============================================================
        books.add(b("Hackers: Heroes of the Computer Revolution", "Steven Levy", "History", 4.5, 15,
                "The story of the computer revolution."));
        books.add(b("The Cathedral and the Bazaar", "Eric S. Raymond", "History", 4.3, 12,
                "Musings on Linux and open source."));
        books.add(b("Coders at Work", "Peter Seibel", "History", 4.5, 14,
                "Reflections on the craft of programming."));
        books.add(b("Founders at Work", "Jessica Livingston", "History", 4.6, 12,
                "Stories of startups' early days."));
        books.add(b("The Soul of a New Machine", "Tracy Kidder", "History", 4.7, 10,
                "The Pulitzer Prize-winning story of a computer."));
        books.add(b("Dealers of Lightning", "Michael A. Hiltzik", "History", 4.6, 8,
                "Xerox PARC and the dawn of the computer age."));
        books.add(b("Steve Jobs", "Walter Isaacson", "History", 4.6, 25,
                "The exclusive biography."));
        books.add(b("The Everything Store", "Brad Stone", "History", 4.4, 15,
                "Jeff Bezos and the age of Amazon."));
        books.add(b("Elon Musk", "Walter Isaacson", "History", 4.5, 18,
                "The biography of the Tesla and SpaceX CEO."));
        books.add(b("Snow Crash", "Neal Stephenson", "History", 4.5, 14,
                "A science fiction novel that shaped cyberpunk."));

        // Save all books
        bookRepository.saveAll(books);
        log.info("✅ {} books loaded successfully!", books.size());
    }

    // ============================================================
    // Helper to build a Book quickly
    // ============================================================
    private Book b(String title, String author, String category,
                   double rating, int borrows, String description) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setRating(rating);
        book.setTotalBorrows(borrows);
        book.setDescription(description);
        book.setQuantity(5);
        book.setAvailableQuantity(5);
        return book;
    }
}