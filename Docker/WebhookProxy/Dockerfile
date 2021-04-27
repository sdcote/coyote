FROM coyote
MAINTAINER Steve Cote <sdcote@gmail.com> 

# Add the files from the build context
ADD opt opt

# Expose the HTTP server port
EXPOSE 80

# Run the server
CMD ["webhookproxy"]

# EOF