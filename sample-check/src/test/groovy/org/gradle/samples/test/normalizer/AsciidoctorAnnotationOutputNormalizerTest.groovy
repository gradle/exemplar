package org.gradle.samples.test.normalizer

import org.gradle.samples.executor.ExecutionMetadata
import spock.lang.Specification
import spock.lang.Subject

@Subject(AsciidoctorAnnotationOutputNormalizer)
class AsciidoctorAnnotationOutputNormalizerTest extends Specification {
    def "removes Asciidoctor annotation"() {
        given:
        OutputNormalizer normalizer = new AsciidoctorAnnotationOutputNormalizer()
        String input = """
            |./build/install
            |├── main
            |│   └── debug
            |│       ├── building-cpp-applications      // <1>
            |│       └── lib
            |│           └── building-cpp-applications  // <2>
            |└── test
            |    ├── building-cpp-applicationsTest      // <1>
            |    └── lib
            |        └── building-cpp-applicationsTest  // <3>
            |
            |5 directories, 4 files""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.contains('// <1>')
        !result.contains('// <2>')
        !result.contains('// <3>')
    }

    def "strip trailing whitespace for aligning Asciidoctor annotation"() {
        given:
        OutputNormalizer normalizer = new AsciidoctorAnnotationOutputNormalizer()
        String input = """
            |./build/install
            |├── main
            |│   └── debug
            |│       ├── building-cpp-applications      // <1>
            |│       └── lib
            |│           └── building-cpp-applications  // <2>
            |└── test
            |    ├── building-cpp-applicationsTest      // <1>
            |    └── lib
            |        └── building-cpp-applicationsTest  // <3>
            |
            |5 directories, 4 files""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        !(input =~ /\s+$/).find()
        def result = normalizer.normalize(input, executionMetadata)
        !result.contains('// <1>')
        !(result =~ /\s+$/).find()
    }

    def "does not remove leading new lines"() {
        given:
        OutputNormalizer normalizer = new AsciidoctorAnnotationOutputNormalizer()
        String input = """
            |./build/install
            |├── main
            |│   └── debug
            |│       ├── building-cpp-applications      // <1>
            |│       └── lib
            |│           └── building-cpp-applications  // <2>
            |└── test
            |    ├── building-cpp-applicationsTest      // <1>
            |    └── lib
            |        └── building-cpp-applicationsTest  // <3>
            |
            |5 directories, 4 files
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.startsWith('\n')
    }

    def "does not remove trailing new lines"() {
        given:
        OutputNormalizer normalizer = new AsciidoctorAnnotationOutputNormalizer()
        String input = """
            |./build/install
            |├── main
            |│   └── debug
            |│       ├── building-cpp-applications      // <1>
            |│       └── lib
            |│           └── building-cpp-applications  // <2>
            |└── test
            |    ├── building-cpp-applicationsTest      // <1>
            |    └── lib
            |        └── building-cpp-applicationsTest  // <3>
            |
            |5 directories, 4 files
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.endsWith('\n')
    }
}
