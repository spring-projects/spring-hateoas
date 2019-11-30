#!/bin/bash

function usage() {
  echo
  echo "NAME"
  echo "migrate-to-1.0.sh - Migrate a codebase from Spring HATEOAS 0.x to 1.0."
  echo
  echo "This script attempts to adjust a codebase for the package and type refactorings"
  echo "made for Spring HATEOAS 1.0."
  echo
  echo "SYNOPSIS"
  echo "migrate-to-1.0.sh [Pattern]"
  echo
  echo "OPTIONS"
  echo " -h --help    this help"
  echo " [Pattern]    a find pattern, default to *.java if you don't provide a pattern"
  echo "              don't forget to enclose your pattern with double quotes \"\" to "
  echo "              avoid pattern to be expanded by your shell prematurely."
  echo
  echo "EXAMPLE"
  echo " migrate-to-1.0.sh \"*IT.java\""
  exit 0
}

if [ "$1" == "-h" -o "$1" == "--help" ] ;
then
 usage
fi

FILES_PATTERN=${1:-*.java}

echo ''
echo "Migrating Spring HATEOAS references to 1.0 for files : $FILES_PATTERN"
echo ''

FILES=`find . -name "$FILES_PATTERN"`

EXPRESSION="s/Link\.REL_SELF/IanaLinkRelations.SELF/g;\
s/Link\.REL_PREV/IanaLinkRelations.PREV/g;\
s/Link\.REL_NEXT/IanaLinkRelations.NEXT/g;\
\
s/hateoas\.EntityLinks/hateoas.server.EntityLinks/g;\
s/hateoas\.JsonPathLinkDiscoverer/hateoas.client.JsonPathLinkDiscoverer/g;\
s/hateoas\.LinkBuilder/hateoas.server.LinkBuilder/g;\
s/hateoas\.LinkDiscoverer/hateoas.client.LinkDiscoverer/g;\
s/hateoas\.LinkDiscoverers/hateoas.client.LinkDiscoverers/g;\
s/hateoas\.MethodLinkBuilderFactory/hateoas.server.MethodLinkBuilderFactory/g;\
s/hateoas\.RelProvider/hateoas.server.LinkRelationProvider/g;\
s/hateoas\.ResourceAssembler/hateoas.server.ResourceAssembler/g;\
s/hateoas\.ResourceProcessor/hateoas.server.ResourceProcessor/g;\
s/hateoas\.VndErrors/hateoas.mediatype.vnderrors.VndErrors/g;\
s/hateoas\.VndErrors.VndError/hateoas.mediatype.vnderrors.VndErrors.VndError/g;\
\
s/hateoas\.alps/hateoas.mediatype.alps/g;\
\
s/hateoas\.core\.AbstractEntityLinks/hateoas.server.core.AbstractEntityLinks/g;\
s/hateoas\.core\.AnnotationAttribute/hateoas.server.core.AnnotationAttribute/g;\
s/hateoas\.core\.AnnotationMappingDiscoverer/hateoas.server.core.AnnotationMappingDiscoverer/g;\
s/hateoas\.core\.DefaultRelProvider/hateoas.server.core.DefaultLinkRelationProvider/g;\
s/hateoas\.core\.EmbeddedWrapper/hateoas.server.core.EmbeddedWrapper/g;\
s/hateoas\.core\.EvoInflectorRelProvider/hateoas.server.core.EvoInflectorLinkRelationProvider/g;\
s/hateoas\.core\.JsonPathLinkDiscoverer/hateoas.client.JsonPathLinkDiscoverer/g;\
s/hateoas\.core\.LinkBuilderSupport/hateoas.server.core.LinkBuilderSupport/g;\
s/hateoas\.core\.MethodParameters/hateoas.server.core.MethodParameters/g;\
s/hateoas\.core\.Relation/hateoas.server.core.Relation/g;\
\
s/hateoas\.hal/hateoas.mediatype.hal/g;\
s/HAL_JSON_UTF8/HAL_JSON/g;\
\
s/hateoas\.mvc\.BasicLinkBuilder/hateoas.server.mvc.BasicLinkBuilder/g;\
s/hateoas\.mvc\.ControllerLinkBuilder/hateoas.server.mvc.WebMvcLinkBuilder/g;\
s/hateoas\.mvc\.ControllerLinkBuilderFactory/hateoas.server.mvc.WebMvcLinkBuilderFactory/g;\
s/hateoas\.mvc\.HeaderLinksResponseEntity/hateoas.server.core.HeaderLinksResponseEntity/g;\
s/hateoas\.mvc\.ResourceAssemblerSupport/hateoas.server.mvc.RepresentationModelAssemblerSupport/g;\
s/hateoas\.mvc\.ResourceProcessorInvoker/hateoas.server.mvc.ResourceProcessorInvoker/g;\
s/hateoas\.mvc\.ResourceProcessorInvokingHandlerAdapter/hateoas.server.mvc.ResourceProcessorInvokingHandlerAdapter/g;\
s/hateoas\.mvc\.TypeConstrainedMappingJackson2HttpMessageConverter/hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter/g;\
s/hateoas\.mvc\.UriComponentsContributor/hateoas.server.mvc.UriComponentsContributor/g;\
s/ControllerLinkBuilderFactory/WebMvcLinkBuilderFactory/g;\
s/ControllerLinkBuilder/WebMvcLinkBuilder/g;\
\
s/\([^a-z]\)ResourceProcessorInvokingHandlerAdapter\([ ><,#();\.]\)/\1RepresentationModelProcessorInvokingHandlerAdapter\2/g;\
s/\([^a-z]\)ResourceProcessorInvoker\([ ><,#();\.]\)/\1RepresentationModelProcessorInvoker\2/g;\
s/\([^a-z]\)ResourceProcessor\([ ><,#();\.]\)/\1RepresentationModelProcessor\2/g;\
\
s/ ResourceAssembler / RepresentationModelAssembler /g;\
s/\.ResourceAssembler;/.RepresentationModelAssembler;/g;\
s/ResourceAssembler\([<#]\)/RepresentationModelAssembler\1/g;\
s/IdentifiableResourceAssemblerSupport/IdentifiableRepresentationModelAssemblerSupport/g;\
s/toResource(/toModel(/g;\
s/toResources(/toCollectionModel(/g;\
\
s/\([^a-z]\)ResourceSupport\([ ><,#();:\.]\)/\1RepresentationModel\2/g;\
s/\([^a-z]\)PagedResources\([ ><,#();:\.]\)/\1PagedModel\2/g;\
s/\([^a-z]\)Resources\([ ><,#();:\.]\)/\1CollectionModel\2/g;\
s/\([^a-z]\)Resource\([ ><,#();:\.]\)/\1EntityModel\2/g;\
s/\([^a-z]\)RelProvider\([ ><,#();:\.]\)/\1LinkRelationProvider\2/g;\
\
s/linkForSingleResource/linkForItemResource/g;\
s/linkToSingleResource/linkToItemResource/g;\
"

#EXPRESSION="s/[\s\.]PagedResources\([;<]\)/.PagedRepresentationModel\1/g;"

for FILE in $FILES
do
    echo "Adapting $FILE"
    # echo $EXPRESSION
    sed -i "" -e "$EXPRESSION" $FILE
done

echo
echo 'Done!'
echo
echo 'If you have used link relation constants defined in Link (like Link.REL_SELF) in your '
echo 'codebase, you will have to trigger an organize imports in your IDE to make sure the'
echo 'now referenced IanaLinkRelations gets imported.'
echo
echo "Also, if you were referring to core Spring's Resource type you might see invalid migrations"
echo "as there's no way for us to differentiate that from Spring HATEOAS Resource type."
echo
echo 'After that, review your changes, commit and code on! \ö/'
